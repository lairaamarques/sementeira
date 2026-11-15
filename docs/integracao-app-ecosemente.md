# Integração app EcoSemente → sementeira web

Guia para ligar o app Android **EcoSemente** (coleta offline das matrizes) ao
backend **sementeira** (Spring Boot). Corresponde ao caso de uso **UC06 – Sincronizar
dados de campo** do TCC.

**Escopo desta etapa:**
- só as **matrizes** (tabela `semente` do app, junto com o nome científico) são enviadas;
- só no sentido **app → web**;
- **fotos ficam para depois**, porque o `caminho_imagem` do app é um endereço local do celular e não serve no servidor;
- **exclusões** feitas no app ainda **não** são enviadas.

---

## 1. Como funciona

```
 [Celular, offline]                          [Servidor sementeira]
 cadastra/edita matriz
   └─ grava no SQLite com
      uuid + atualizado_em + sincronizado=0
                     … volta a ter internet …
 botão "Sincronizar"
   └─ envia as pendentes  ── POST /api/sync/matrizes ──►  grava ou atualiza pelo uuid
   ◄── um resultado por matriz ─────────────────────────  (reenviar não duplica)
   └─ marca como sincronizado=1
```

- **`uuid`**: o celular gera um identificador único (`UUID.randomUUID()`) para cada
  matriz. Ele é a chave da sincronização. O `id` do SQLite não serve, porque dois
  celulares podem ter a "matriz 1".
- **Reenviar é seguro.** Se a internet cair no meio do envio, basta mandar de novo.
  O servidor reconhece o uuid e não duplica a matriz.
- **`atualizado_em`**: se o servidor já tem uma versão igual ou mais nova da matriz,
  ele a **ignora**. Assim, um envio atrasado não sobrescreve um dado mais novo.
- **Nome científico vai como texto**, por exemplo `"Carapa guianensis"`, e não como id.
  O servidor procura esse nome (sem diferenciar maiúsculas) e o cria se ainda não existir.

---

## 2. Contrato da API

### `POST /api/sync/matrizes`

Requisição (no máximo **500** matrizes por envio):

```json
{
  "dispositivo": "Celular da Fani",
  "matrizes": [
    {
      "uuid": "3f2b8c1e-6a4d-4e1f-9b7a-2c5d8e9f0a1b",
      "nome": "Andiroba",
      "nomeCientifico": "Carapa guianensis",
      "tempoMedioColheita": 180,
      "epocaInicio": "Janeiro",
      "epocaFim": "Abril",
      "tipoCultivo": "Natural",
      "tamanhoPorte": "Grande",
      "latitude": -3.1019,
      "longitude": -60.025,
      "atualizadoEm": "2026-11-12T14:30:00"
    }
  ]
}
```

| Campo | Obrigatório | Observação |
|---|---|---|
| `uuid` | sim | até 36 caracteres |
| `nome` | sim | nome popular |
| `nomeCientifico` | não | texto; criado no servidor se não existir |
| `latitude` / `longitude` | não | entre -90 e 90 / entre -180 e 180. **Mande `null` se não houver GPS, nunca `0`** |
| `atualizadoEm` | recomendado | `yyyy-MM-dd'T'HH:mm:ss`, horário local do celular |
| demais campos | não | iguais aos da tabela `semente` do app |

Resposta `200 OK` (com um item em `resultados` para cada matriz, na mesma ordem do envio):

```json
{
  "recebidas": 1, "criadas": 1, "atualizadas": 0, "ignoradas": 0, "erros": 0,
  "sincronizadoEm": "2026-11-12T18:40:12",
  "resultados": [
    { "uuid": "3f2b8c1e-…", "id": 17, "situacao": "CRIADA", "mensagem": null }
  ]
}
```

| `situacao` | O que o app deve fazer |
|---|---|
| `CRIADA`, `ATUALIZADA`, `IGNORADA` | marcar como sincronizada |
| `ERRO` | manter pendente e mostrar a `mensagem` ao usuário |

Uma matriz com `ERRO` **não** impede que as outras do mesmo envio sejam salvas.

### `GET /api/sync/status`

```json
{ "ultimaSincronizacao": "2026-11-12T18:40:12", "dispositivo": "Celular da Fani", "totalMatrizes": 17 }
```

O web usa essa rota no selo do cabeçalho, que mostra quando foi o último envio do app.

---

## 3. Mudanças no app

> Os trechos abaixo seguem o código atual do app (Java 8, minSdk 24, Retrofit 2.9).
> Não use `LocalDateTime`: ele exige Android 8 (API 26). As datas vão como texto.

### 3.1 Banco local: versão 9, **sem apagar dados**

⚠️ **Importante:** hoje o `onUpgrade` do `BDEcoSemente` faz `DROP TABLE` em tudo. Para um
app de **coleta offline**, isso significa que, ao instalar uma versão nova, todas as
matrizes ainda não enviadas **se perdem**. A migração precisa usar `ALTER TABLE`.

```java
private static final int DATABASE_VERSION = 9;

// Em onCreate, na tabela semente, acrescente as colunas:
//   "uuid TEXT UNIQUE," +
//   "atualizado_em TEXT," +
//   "sincronizado INTEGER NOT NULL DEFAULT 0," +

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 8) {
        // Versões muito antigas: mantém o comportamento anterior.
        db.execSQL("DROP TABLE IF EXISTS item_venda");
        db.execSQL("DROP TABLE IF EXISTS semente");
        db.execSQL("DROP TABLE IF EXISTS venda");
        db.execSQL("DROP TABLE IF EXISTS comprador");
        db.execSQL("DROP TABLE IF EXISTS nome_cientifico");
        onCreate(db);
        return;
    }
    if (oldVersion < 9) {
        db.execSQL("ALTER TABLE semente ADD COLUMN uuid TEXT");
        db.execSQL("ALTER TABLE semente ADD COLUMN atualizado_em TEXT");
        db.execSQL("ALTER TABLE semente ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 0");
        // Gera uuid para as matrizes que já existem no aparelho
        Cursor c = db.rawQuery("SELECT id FROM semente", null);
        while (c.moveToNext()) {
            ContentValues cv = new ContentValues();
            cv.put("uuid", java.util.UUID.randomUUID().toString());
            cv.put("atualizado_em", SyncDatas.agora());
            db.update("semente", cv, "id = ?", new String[]{String.valueOf(c.getLong(0))});
        }
        c.close();
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_semente_uuid ON semente(uuid)");
    }
}
```

`SyncDatas`, uma classe auxiliar nova:

```java
public final class SyncDatas {
    private static final SimpleDateFormat ISO =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    public static synchronized String agora() { return ISO.format(new Date()); }
}
```

### 3.2 `Semente` e `SementeDAO`

Na entidade `Semente`, adicione `private String uuid;` e `private String atualizadoEm;`,
com getters e setters.

No `SementeDAO`:

```java
public void insert(Semente semente){
    ContentValues cv = sementeToContentValues(semente);
    cv.put("uuid", java.util.UUID.randomUUID().toString());
    cv.put("atualizado_em", SyncDatas.agora());
    cv.put("sincronizado", 0);
    sqLiteDatabase.insert("semente", null, cv);
}

public void update(long id, Semente semente){
    ContentValues cv = sementeToContentValues(semente);
    cv.put("atualizado_em", SyncDatas.agora());
    cv.put("sincronizado", 0);            // editou → precisa enviar de novo
    sqLiteDatabase.update("semente", cv, "id = ?", new String[]{Long.toString(id)});
}

/** Matrizes que ainda não foram enviadas, já com o nome científico em texto. */
public List<MatrizSyncDTO> getPendentesSync() {
    List<MatrizSyncDTO> lista = new ArrayList<>();
    Cursor c = sqLiteDatabase.rawQuery(
        "SELECT s.*, nc.nome AS nc_nome FROM semente s " +
        "LEFT JOIN nome_cientifico nc ON nc.id = s.nome_cientifico_id " +
        "WHERE s.sincronizado = 0", null);
    while (c.moveToNext()) {
        lista.add(MatrizSyncDTO.deCursor(c));
    }
    c.close();
    return lista;
}

/**
 * Marca como enviada SÓ se não foi editada durante o envio
 * (compara o atualizado_em que foi mandado).
 */
public void marcarSincronizada(String uuid, String atualizadoEmEnviado) {
    ContentValues cv = new ContentValues();
    cv.put("sincronizado", 1);
    sqLiteDatabase.update("semente", cv, "uuid = ? AND atualizado_em = ?",
            new String[]{uuid, atualizadoEmEnviado});
}
```

### 3.3 DTOs de sincronização

```java
public class MatrizSyncDTO {
    String uuid, nome, nomeCientifico, epocaInicio, epocaFim, tipoCultivo, tamanhoPorte, atualizadoEm;
    Integer tempoMedioColheita;
    Double latitude, longitude;

    @SuppressLint("Range")
    static MatrizSyncDTO deCursor(Cursor c) {
        MatrizSyncDTO m = new MatrizSyncDTO();
        m.uuid = c.getString(c.getColumnIndex("uuid"));
        m.nome = c.getString(c.getColumnIndex("nome"));
        m.nomeCientifico = c.getString(c.getColumnIndex("nc_nome"));
        m.epocaInicio = c.getString(c.getColumnIndex("epoca_inicio"));
        m.epocaFim = c.getString(c.getColumnIndex("epoca_fim"));
        m.tipoCultivo = c.getString(c.getColumnIndex("tipo_cultivo"));
        m.tamanhoPorte = c.getString(c.getColumnIndex("tamanho_porte"));
        m.atualizadoEm = c.getString(c.getColumnIndex("atualizado_em"));
        int iTempo = c.getColumnIndex("tempo_medio_colheita");
        m.tempoMedioColheita = c.isNull(iTempo) ? null : c.getInt(iTempo);
        // getDouble() devolve 0 quando é NULL — isso viraria um ponto no oceano!
        int iLat = c.getColumnIndex("latitude"), iLng = c.getColumnIndex("longitude");
        m.latitude = c.isNull(iLat) ? null : c.getDouble(iLat);
        m.longitude = c.isNull(iLng) ? null : c.getDouble(iLng);
        return m;
    }
}

public class SyncRequisicaoDTO {
    String dispositivo;
    List<MatrizSyncDTO> matrizes;
    public SyncRequisicaoDTO(String dispositivo, List<MatrizSyncDTO> matrizes) {
        this.dispositivo = dispositivo; this.matrizes = matrizes;
    }
}

public class SyncRespostaDTO {
    int recebidas, criadas, atualizadas, ignoradas, erros;
    String sincronizadoEm;
    List<Resultado> resultados;
    public static class Resultado { String uuid; Long id; String situacao; String mensagem; }
}
```

### 3.4 Interface Retrofit e endereço do servidor

```java
public interface SyncAPI {
    @POST("sync/matrizes")
    Call<SyncRespostaDTO> enviarMatrizes(@Body SyncRequisicaoDTO requisicao);
}
```

No `RetrofitService`, a `baseUrl` hoje aponta para `http://10.100.81.89:8080/api/`. O
sementeira roda na porta **8081**:

- **emulador do Android Studio:** `http://10.0.2.2:8081/api/` (o `10.0.2.2` é o próprio PC);
- **celular de verdade:** `http://<IP do PC na rede Wi-Fi>:8081/api/`. O celular e o PC
  precisam estar na mesma rede, e o firewall do Windows tem que liberar a porta 8081.

O manifest já tem `INTERNET` e `usesCleartextTraffic="true"`, então o HTTP sem TLS funciona.

### 3.5 Sincronizador (botão "Sincronizar")

```java
public class SincronizadorMatrizes {

    public interface Callback { void concluido(String mensagem); }

    public static void sincronizar(Context ctx, Callback cb) {
        SementeDAO dao = new SementeDAO(ctx);
        dao.open();
        List<MatrizSyncDTO> pendentes = dao.getPendentesSync();
        dao.close();
        if (pendentes.isEmpty()) { cb.concluido("Nada para sincronizar."); return; }

        SyncAPI api = RetrofitService.createService(SyncAPI.class);
        api.enviarMatrizes(new SyncRequisicaoDTO(android.os.Build.MODEL, pendentes))
           .enqueue(new retrofit2.Callback<SyncRespostaDTO>() {
               @Override public void onResponse(Call<SyncRespostaDTO> call, Response<SyncRespostaDTO> resp) {
                   if (!resp.isSuccessful() || resp.body() == null) {
                       cb.concluido("Servidor recusou o envio (HTTP " + resp.code() + ")."); return;
                   }
                   SementeDAO d = new SementeDAO(ctx);
                   d.open();
                   for (int i = 0; i < resp.body().resultados.size(); i++) {
                       SyncRespostaDTO.Resultado r = resp.body().resultados.get(i);
                       if (!"ERRO".equals(r.situacao)) {
                           d.marcarSincronizada(r.uuid, pendentes.get(i).atualizadoEm);
                       }
                   }
                   d.close();
                   SyncRespostaDTO b = resp.body();
                   cb.concluido(b.criadas + " novas, " + b.atualizadas + " atualizadas"
                           + (b.erros > 0 ? ", " + b.erros + " com erro" : "") + ".");
               }
               @Override public void onFailure(Call<SyncRespostaDTO> call, Throwable t) {
                   cb.concluido("Sem conexão com o servidor. Os dados continuam salvos no celular.");
               }
           });
    }
}
```

Exemplo de uso em uma tela (no `AccountFragment` ou num item de menu):

```java
SincronizadorMatrizes.sincronizar(requireContext(),
        msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
```

> Com mais de 500 matrizes pendentes, divida o envio em lotes de até 500.

---

## 4. Testar sem o app

Com o backend rodando, no PowerShell:

```powershell
$corpo = '{"dispositivo":"Teste","matrizes":[{"uuid":"teste-0001","nome":"Andiroba","nomeCientifico":"Carapa guianensis","latitude":-3.1,"longitude":-60.0,"atualizadoEm":"2026-11-12T10:00:00"}]}'
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/sync/matrizes -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($corpo))
```

Depois disso, o selo do cabeçalho do web mostra "App Android sincronizado agora mesmo" e
o card **Matrizes Mapeadas** do Painel passa a contar a matriz. Com o filtro por espécie,
o card conta só as matrizes cujo nome científico é igual ao da espécie no Catálogo.

---

## 5. Próximas etapas

1. **Fotos**: um endpoint `POST /api/sync/matrizes/{uuid}/foto` (multipart), chamado
   depois que a matriz for sincronizada.
2. **Exclusões**: marcar a matriz como excluída no app (em vez de apagar a linha) e
   enviar `excluida: true`.
3. **Sincronização automática**: usar o WorkManager para enviar sozinho quando houver
   rede, além do botão.

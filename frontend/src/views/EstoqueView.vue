<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { especiesApi, estoqueApi, matrizesApi, mensagemDeErro } from '@/services/api'
import { mostrarToast } from '@/services/toast'
import { numero, data, nomeProduto, rotuloLote } from '@/utils/formato'
import ModalConfirmacao from '@/components/ModalConfirmacao.vue'

const especies = ref([])
const matrizes = ref([])
const lotes = ref([])
const saidas = ref([])
const familias = ref([])
const carregando = ref(true)

const entradaVazia = () => ({ especieId: '', quantidade: null, unidade: 'Sacas', familiaProdutora: '', sementeId: null })
const entrada = reactive(entradaVazia())

const nomeCientificoDaMatriz = (m) => m?.nomeCientifico?.nome ?? null

const matrizesDaEspecie = computed(() => {
  const especie = especies.value.find(e => e.id === entrada.especieId)
  if (!especie) return []
  const alvo = especie.nomeCientifico.toLowerCase()
  return matrizes.value.filter(m => {
    const nc = nomeCientificoDaMatriz(m)
    return !nc || nc.toLowerCase() === alvo
  })
})

watch(() => entrada.especieId, () => { entrada.sementeId = null })

const rotuloMatriz = (m) =>
  `Matriz #${m.id} — ${m.nome}${m.latitude != null ? ` (${m.latitude.toFixed(4)}, ${m.longitude.toFixed(4)})` : ''}`
const errosEntrada = reactive({})
const salvandoEntrada = ref(false)

const saidaVazia = () => ({ estoqueId: '', tipo: 'BAIXA', quantidade: null, observacao: '' })
const saida = reactive(saidaVazia())
const salvandoSaida = ref(false)

const confirmacao = ref(null)

const lotesComSaldo = computed(() => lotes.value.filter(l => Number(l.saldo) > 0))
const loteSaida = computed(() => lotes.value.find(l => l.id === saida.estoqueId))
const saidaExcedeSaldo = computed(() =>
  loteSaida.value && saida.quantidade > 0 && saida.quantidade > Number(loteSaida.value.saldo))

async function carregar() {
  try {
    const [esp, lts, sds, fams] = await Promise.all([
      especiesApi.listar(), estoqueApi.listar(), estoqueApi.listarSaidas(), estoqueApi.listarFamilias(),
    ])
    especies.value = esp
    matrizes.value = await matrizesApi.listar().catch(() => [])
    lotes.value = lts
    saidas.value = sds
    familias.value = fams
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

function validarEntrada() {
  for (const k of Object.keys(errosEntrada)) delete errosEntrada[k]
  if (!entrada.especieId) errosEntrada.especieId = 'Selecione a espécie'
  if (!entrada.quantidade || entrada.quantidade <= 0) errosEntrada.quantidade = 'Informe a quantidade'
  if (!entrada.familiaProdutora.trim()) errosEntrada.familiaProdutora = 'Informe a família produtora'
  return Object.keys(errosEntrada).length === 0
}

async function lancarEntrada() {
  if (!validarEntrada()) return
  salvandoEntrada.value = true
  try {
    await estoqueApi.lancarEntrada(entrada)
    Object.assign(entrada, entradaVazia())
    mostrarToast('📦 Inventário atualizado com sucesso!')
    await carregar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    salvandoEntrada.value = false
  }
}

async function registrarSaida() {
  if (!saida.estoqueId || !saida.quantidade || saidaExcedeSaldo.value) return
  salvandoSaida.value = true
  try {
    const { estoqueId, ...dados } = saida
    await estoqueApi.registrarSaida(estoqueId, dados)
    mostrarToast(saida.tipo === 'PERDA' ? 'Perda registrada.' : 'Baixa registrada.', 'destaque')
    Object.assign(saida, saidaVazia())
    await carregar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    salvandoSaida.value = false
  }
}

function pedirDesfazerEntrada(lote) {
  confirmacao.value = {
    titulo: 'Desfazer esta entrada?',
    mensagem: `O lote "${rotuloLote(lote)}" será removido do inventário. Só é possível se ele ainda não teve vendas ou baixas.`,
    acao: async () => {
      await estoqueApi.desfazerEntrada(lote.id)
      mostrarToast('Entrada desfeita.', 'destaque')
    },
  }
}

function pedirDesfazerSaida(mov) {
  confirmacao.value = {
    titulo: 'Desfazer esta saída?',
    mensagem: `${numero(mov.quantidade)} ${mov.estoque.unidade} voltarão para o lote de ${nomeProduto(mov.estoque.especie)}.`,
    acao: async () => {
      await estoqueApi.desfazerSaida(mov.id)
      mostrarToast('Saída desfeita. Saldo devolvido ao lote.')
    },
  }
}

async function executarConfirmacao() {
  const { acao } = confirmacao.value
  confirmacao.value = null
  try {
    await acao()
    await carregar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  }
}
</script>

<template>
  <div>
    <div class="card full-col">
      <div class="card-header">
        <div class="card-title">
          <div class="card-title-icon">📥</div>
          Entrada de Insumos — Agricultura Familiar
        </div>
      </div>
      <div class="card-body">
        <div v-if="!carregando && especies.length === 0" class="aviso-inline" style="margin:0 0 12px">
          Cadastre ao menos uma espécie no Catálogo antes de lançar estoque.
        </div>
        <form novalidate @submit.prevent="lancarEntrada">
          <div class="form-grid">
            <div class="form-group">
              <label for="est-especie">Espécie Regulamentada</label>
              <select id="est-especie" v-model="entrada.especieId" class="ui-select" :class="{ invalido: errosEntrada.especieId }">
                <option value="" disabled>Selecione…</option>
                <option v-for="e in especies" :key="e.id" :value="e.id">{{ e.nomePopular }}</option>
              </select>
              <span v-if="errosEntrada.especieId" class="msg-erro">{{ errosEntrada.especieId }}</span>
            </div>
            <div class="form-group">
              <label for="est-qtd">Quantidade Bruta</label>
              <input id="est-qtd" v-model.number="entrada.quantidade" type="number" min="0" step="any" class="ui-input"
                     :class="{ invalido: errosEntrada.quantidade }" placeholder="Ex: 15">
              <span v-if="errosEntrada.quantidade" class="msg-erro">{{ errosEntrada.quantidade }}</span>
            </div>
            <div class="form-group">
              <label for="est-unidade">Unidade de Medida</label>
              <select id="est-unidade" v-model="entrada.unidade" class="ui-select">
                <option value="Sacas">Sacas (Sementes)</option>
                <option value="Unidades">Unidades (Mudas)</option>
                <option value="Quilos">Quilos (Polpa)</option>
                <option value="Litros">Litros (Óleo)</option>
              </select>
            </div>
            <div class="form-group">
              <label for="est-familia">Família Produtora / Origem</label>
              <input id="est-familia" v-model="entrada.familiaProdutora" type="text" list="familias" class="ui-input"
                     :class="{ invalido: errosEntrada.familiaProdutora }" placeholder="Ex: Família Souza – Rio Negro">
              <datalist id="familias">
                <option v-for="f in familias" :key="f.id" :value="f.nome" />
              </datalist>
              <span v-if="errosEntrada.familiaProdutora" class="msg-erro">{{ errosEntrada.familiaProdutora }}</span>
            </div>
            <div class="form-group">
              <label for="est-matriz">Matriz de Origem (opcional)</label>
              <select id="est-matriz" v-model="entrada.sementeId" class="ui-select" :disabled="!entrada.especieId">
                <option :value="null">— Não informar —</option>
                <option v-for="m in matrizesDaEspecie" :key="m.id" :value="m.id">{{ rotuloMatriz(m) }}</option>
              </select>
              <span v-if="entrada.especieId && matrizesDaEspecie.length === 0" class="sub">
                Nenhuma matriz desta espécie sincronizada pelo app ainda.
              </span>
            </div>
          </div>
          <div class="form-footer">
            <button type="submit" class="btn btn-accent" :disabled="salvandoEntrada">➕ Lançar em Inventário</button>
          </div>
        </form>
      </div>
    </div>

    <div class="card full-col">
      <div class="card-header">
        <div class="card-title">
          <div class="card-title-icon">📦</div>
          Inventário Comercial Atualizado
        </div>
      </div>
      <div v-if="carregando" class="carregando">Carregando…</div>
      <div v-else-if="lotes.length === 0" class="vazio">Nenhum lote no inventário.</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Recurso / Espécie</th>
              <th>Saldo Físico</th>
              <th>Família Produtora</th>
              <th>Entrada</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in lotes" :key="l.id">
              <td>
                <strong>{{ nomeProduto(l.especie) }}</strong>
                <div v-if="l.semente" class="sub">🌳 Matriz #{{ l.semente.id }} — {{ l.semente.nome }}</div>
                <div v-if="!l.especie.ativo" class="sub">espécie inativa</div>
              </td>
              <td class="mono">
                {{ numero(l.saldo) }} {{ l.unidade }}
                <div class="sub">de {{ numero(l.quantidadeInicial) }} lançadas</div>
              </td>
              <td>{{ l.familiaProdutora.nome }}</td>
              <td class="mono">{{ data(l.dataEntrada) }}</td>
              <td>
                <span v-if="Number(l.saldo) === 0" class="badge badge-muted">Esgotado</span>
                <span v-else class="badge" :class="l.status === 'Reservado' ? 'badge-warning' : 'badge-success'">{{ l.status }}</span>
              </td>
              <td>
                <div class="acoes">
                  <button class="btn btn-danger-ghost btn-sm" title="Remover entrada lançada por engano"
                          @click="pedirDesfazerEntrada(l)">Desfazer</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <div class="card-title-icon">📤</div>
            Dar Baixa / Registrar Perda
          </div>
        </div>
        <div class="card-body">
          <form @submit.prevent="registrarSaida">
            <div class="form-grid" style="grid-template-columns:1fr;">
              <div class="form-group">
                <label for="sai-lote">Lote</label>
                <select id="sai-lote" v-model="saida.estoqueId" class="ui-select" required>
                  <option value="" disabled>Selecione…</option>
                  <option v-for="l in lotesComSaldo" :key="l.id" :value="l.id">{{ rotuloLote(l) }}</option>
                </select>
              </div>
              <div class="form-grid" style="grid-template-columns:1fr 1fr;">
                <div class="form-group">
                  <label for="sai-tipo">Motivo</label>
                  <select id="sai-tipo" v-model="saida.tipo" class="ui-select">
                    <option value="BAIXA">Dar Baixa</option>
                    <option value="PERDA">Registrar Perda</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="sai-qtd">Quantidade</label>
                  <input id="sai-qtd" v-model.number="saida.quantidade" type="number" min="0" step="any" class="ui-input"
                         :class="{ invalido: saidaExcedeSaldo }" required>
                </div>
              </div>
              <div class="form-group">
                <label for="sai-obs">Observação</label>
                <input id="sai-obs" v-model="saida.observacao" type="text" class="ui-input" placeholder="Ex: sementes mofadas">
              </div>
            </div>
            <div v-if="saidaExcedeSaldo" class="aviso-inline">⚠️ Saldo indisponível para esta operação</div>
            <div class="form-footer">
              <button type="submit" class="btn btn-accent btn-full" :disabled="salvandoSaida || saidaExcedeSaldo">Registrar Saída</button>
            </div>
          </form>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <div class="card-title-icon">🧾</div>
            Histórico de Saídas
          </div>
        </div>
        <div v-if="saidas.length === 0" class="vazio">Nenhuma baixa ou perda registrada.</div>
        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr><th>Produto</th><th>Motivo</th><th>Qtd</th><th>Data</th><th></th></tr>
            </thead>
            <tbody>
              <tr v-for="m in saidas" :key="m.id">
                <td>
                  {{ nomeProduto(m.estoque.especie) }}
                  <div v-if="m.observacao" class="sub">{{ m.observacao }}</div>
                </td>
                <td>
                  <span class="badge" :class="m.tipo === 'PERDA' ? 'badge-pending' : 'badge-info'">
                    {{ m.tipo === 'PERDA' ? 'Perda' : 'Baixa' }}
                  </span>
                </td>
                <td class="mono">{{ numero(m.quantidade) }} {{ m.estoque.unidade }}</td>
                <td class="mono">{{ data(m.data) }}</td>
                <td><div class="acoes"><button class="btn btn-ghost btn-sm" @click="pedirDesfazerSaida(m)">Desfazer</button></div></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <ModalConfirmacao
      v-if="confirmacao"
      :titulo="confirmacao.titulo"
      :mensagem="confirmacao.mensagem"
      texto-confirmar="Desfazer"
      @confirmar="executarConfirmacao"
      @cancelar="confirmacao = null"
    />
  </div>
</template>

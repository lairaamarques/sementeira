<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { especiesApi, mensagemDeErro } from '@/services/api'
import { mostrarToast } from '@/services/toast'
import { moeda } from '@/utils/formato'
import ModalConfirmacao from '@/components/ModalConfirmacao.vue'

const especies = ref([])
const carregando = ref(true)
const salvando = ref(false)
const erroForm = ref('')

const formVazio = () => ({ id: null, nomePopular: '', nomeCientifico: '', tipoRecurso: 'Sementes', tempoMaturacao: '', valorMercado: null })
const form = reactive(formVazio())
const editando = computed(() => form.id !== null)

const paraInativar = ref(null)

async function carregar() {
  carregando.value = true
  try {
    especies.value = await especiesApi.listar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

function limpar() {
  Object.assign(form, formVazio())
  erroForm.value = ''
}

function editar(especie) {
  Object.assign(form, especie)
  erroForm.value = ''
}

async function salvar() {
  salvando.value = true
  erroForm.value = ''
  try {
    const { id, ...dados } = form
    if (editando.value) {
      await especiesApi.atualizar(id, dados)
      mostrarToast('✅ Espécie atualizada!')
    } else {
      await especiesApi.cadastrar(dados)
      mostrarToast('✅ Espécie adicionada ao Catálogo!')
    }
    limpar()
    await carregar()
  } catch (e) {
    erroForm.value = mensagemDeErro(e)
  } finally {
    salvando.value = false
  }
}

async function confirmarInativacao() {
  const especie = paraInativar.value
  paraInativar.value = null
  try {
    await especiesApi.inativar(especie.id)
    if (form.id === especie.id) limpar()
    mostrarToast(`${especie.nomePopular} foi inativada.`, 'destaque')
    await carregar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  }
}
</script>

<template>
  <div class="two-col">
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <div class="card-title-icon">📋</div>
          Diretório de Variedades
        </div>
        <span style="font-size:11px;color:var(--text-3)">
          {{ especies.length }} espécie{{ especies.length !== 1 ? 's' : '' }}
        </span>
      </div>
      <div v-if="carregando" class="carregando">Carregando…</div>
      <div v-else-if="especies.length === 0" class="vazio">Nenhuma espécie cadastrada ainda. Use o formulário ao lado.</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome Popular</th>
              <th>Nome Científico</th>
              <th>Recurso</th>
              <th>Maturação</th>
              <th>Valor Mercado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in especies" :key="e.id">
              <td><strong>{{ e.nomePopular }}</strong></td>
              <td><em style="color:var(--text-2)">{{ e.nomeCientifico }}</em></td>
              <td>{{ e.tipoRecurso }}</td>
              <td><span class="badge" :class="/cont/i.test(e.tempoMaturacao) ? 'badge-success' : 'badge-info'">{{ e.tempoMaturacao }}</span></td>
              <td class="mono">{{ e.valorMercado != null ? moeda(e.valorMercado) : '—' }}</td>
              <td>
                <div class="acoes">
                  <button class="btn btn-ghost btn-sm" @click="editar(e)">Editar</button>
                  <button class="btn btn-danger-ghost btn-sm" @click="paraInativar = e">Inativar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <div class="card-title-icon">{{ editando ? '✏️' : '➕' }}</div>
          {{ editando ? 'Editar Espécie' : 'Nova Espécie' }}
        </div>
      </div>
      <div class="card-body">
        <form @submit.prevent="salvar">
          <div class="form-grid" style="grid-template-columns:1fr;">
            <div class="form-group">
              <label for="cat-nome">Nome Popular</label>
              <input id="cat-nome" v-model="form.nomePopular" type="text" class="ui-input" placeholder="Ex: Macaranduba" required>
            </div>
            <div class="form-group">
              <label for="cat-cientifico">Nome Científico</label>
              <input id="cat-cientifico" v-model="form.nomeCientifico" type="text" class="ui-input" placeholder="Ex: Manilkara huberi" required>
            </div>
            <div class="form-group">
              <label for="cat-recurso">Tipo de Recurso</label>
              <select id="cat-recurso" v-model="form.tipoRecurso" class="ui-select" required>
                <option value="Sementes">Sementes / Sacas</option>
                <option value="Mudas">Mudas / Viveiro</option>
                <option value="Frutos">Frutos In Natura</option>
                <option value="Óleo">Óleo / Resina</option>
              </select>
            </div>
            <div class="form-group">
              <label for="cat-tempo">Tempo de Maturação</label>
              <input id="cat-tempo" v-model="form.tempoMaturacao" type="text" class="ui-input" placeholder="Ex: 5 meses" required>
            </div>
            <div class="form-group">
              <label for="cat-valor">Valor de Mercado (R$ por unidade)</label>
              <input id="cat-valor" v-model.number="form.valorMercado" type="number" min="0" step="0.01" class="ui-input" placeholder="Ex: 150">
            </div>
          </div>
          <div v-if="erroForm" class="aviso-inline">⚠️ {{ erroForm }}</div>
          <div class="form-footer">
            <button v-if="editando" type="button" class="btn btn-ghost" @click="limpar">Cancelar</button>
            <button type="submit" class="btn btn-primary" :class="{ 'btn-full': !editando }" :disabled="salvando">
              💾 {{ editando ? 'Salvar Alterações' : 'Guardar Espécie' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <ModalConfirmacao
      v-if="paraInativar"
      :titulo="`Inativar ${paraInativar.nomePopular}?`"
      mensagem="Tem certeza que deseja inativar esta espécie? Ela não aparecerá mais para novos estoques. O histórico de vendas é preservado."
      texto-confirmar="Inativar"
      @confirmar="confirmarInativacao"
      @cancelar="paraInativar = null"
    />
  </div>
</template>

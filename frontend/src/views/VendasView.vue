<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { estoqueApi, vendasApi, mensagemDeErro } from '@/services/api'
import { mostrarToast } from '@/services/toast'
import { numero, moeda, data, nomeProduto, rotuloLote } from '@/utils/formato'
import ModalConfirmacao from '@/components/ModalConfirmacao.vue'

const lotes = ref([])
const vendas = ref([])
const carregando = ref(true)
const salvando = ref(false)
const paraCancelar = ref(null)

const vendaVazia = () => ({ estoqueId: '', quantidade: null, valorTotal: null, comprador: '' })
const venda = reactive(vendaVazia())

const lotesComSaldo = computed(() => lotes.value.filter(l => Number(l.saldo) > 0))
const loteSelecionado = computed(() => lotes.value.find(l => l.id === venda.estoqueId))

const saldoInsuficiente = computed(() =>
  loteSelecionado.value && venda.quantidade > 0 && venda.quantidade > Number(loteSelecionado.value.saldo))

const valorSugerido = computed(() => {
  const vm = loteSelecionado.value?.especie.valorMercado
  return vm != null && venda.quantidade > 0 ? vm * venda.quantidade : null
})

async function carregar() {
  try {
    const [lts, vds] = await Promise.all([estoqueApi.listar(), vendasApi.listar()])
    lotes.value = lts
    vendas.value = vds
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

async function concluir() {
  if (saldoInsuficiente.value) return
  salvando.value = true
  try {
    await vendasApi.registrar(venda)
    Object.assign(venda, vendaVazia())
    mostrarToast('💰 Venda registrada com sucesso!', 'destaque')
    await carregar()
  } catch (e) {
    mostrarToast(mensagemDeErro(e), 'erro')
  } finally {
    salvando.value = false
  }
}

async function confirmarCancelamento() {
  const v = paraCancelar.value
  paraCancelar.value = null
  try {
    await vendasApi.cancelar(v.id)
    mostrarToast('Venda cancelada. Quantidade devolvida ao estoque.')
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
          <div class="card-title-icon">💰</div>
          Formalização de Saída Comercial
        </div>
      </div>
      <div class="card-body">
        <div v-if="!carregando && lotesComSaldo.length === 0" class="aviso-inline" style="margin:0 0 12px">
          Não há produtos com saldo em estoque. Lance entradas na aba Estoque de Recursos.
        </div>
        <form @submit.prevent="concluir">
          <div class="form-grid" style="grid-template-columns:1fr;">
            <div class="form-group">
              <label for="ven-produto">Produto Selecionado</label>
              <select id="ven-produto" v-model="venda.estoqueId" class="ui-select" required>
                <option value="" disabled>Selecione…</option>
                <option v-for="l in lotesComSaldo" :key="l.id" :value="l.id">{{ rotuloLote(l) }}</option>
              </select>
            </div>
            <div class="form-grid" style="grid-template-columns:1fr 1fr;">
              <div class="form-group">
                <label for="ven-qtd">Quantidade{{ loteSelecionado ? ` (${loteSelecionado.unidade})` : '' }}</label>
                <input id="ven-qtd" v-model.number="venda.quantidade" type="number" min="0" step="any" class="ui-input"
                       :class="{ invalido: saldoInsuficiente }" placeholder="Ex: 10" required>
              </div>
              <div class="form-group">
                <label for="ven-valor">Valor Total (R$)</label>
                <input id="ven-valor" v-model.number="venda.valorTotal" type="number" min="0" step="0.01" class="ui-input"
                       :placeholder="valorSugerido != null ? `Sugestão: ${valorSugerido}` : 'Ex: 2500'" required>
              </div>
            </div>
            <div class="form-group">
              <label for="ven-comprador">Comprador / Empresa Parceira</label>
              <input id="ven-comprador" v-model="venda.comprador" type="text" class="ui-input" placeholder="Ex: Cooperativa de Cosméticos" required>
            </div>
          </div>
          <div v-if="saldoInsuficiente" class="aviso-inline">
            ⚠️ Saldo indisponível para esta operação (disponível: {{ numero(loteSelecionado.saldo) }} {{ loteSelecionado.unidade }})
          </div>
          <div class="form-footer">
            <button type="submit" class="btn btn-accent btn-full" :disabled="salvando || saldoInsuficiente">🛒 Concluir Transação</button>
          </div>
        </form>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <div class="card-title-icon">📜</div>
          Histórico de Transações
        </div>
      </div>
      <div v-if="carregando" class="carregando">Carregando…</div>
      <div v-else-if="vendas.length === 0" class="vazio">Nenhuma venda registrada ainda.</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>Produto</th><th>Comprador</th><th>Qtd</th><th>Faturamento</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            <tr v-for="v in vendas" :key="v.id">
              <td>
                {{ nomeProduto(v.estoque.especie) }}
                <div class="sub">{{ data(v.dataVenda) }}</div>
              </td>
              <td>{{ v.comprador }}</td>
              <td class="mono">{{ numero(v.quantidade) }} {{ v.estoque.unidade }}</td>
              <td class="mono"><strong>{{ moeda(v.valorTotal) }}</strong></td>
              <td>
                <span class="badge" :class="v.status === 'Cancelado' ? 'badge-muted' : 'badge-success'">{{ v.status }}</span>
              </td>
              <td>
                <div class="acoes">
                  <button v-if="v.status !== 'Cancelado'" class="btn btn-danger-ghost btn-sm" @click="paraCancelar = v">Cancelar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <ModalConfirmacao
      v-if="paraCancelar"
      titulo="Cancelar esta venda?"
      :mensagem="`${numero(paraCancelar.quantidade)} ${paraCancelar.estoque.unidade} de ${paraCancelar.estoque.especie.nomePopular} voltarão ao estoque e o valor sai do faturamento.`"
      texto-confirmar="Cancelar venda"
      @confirmar="confirmarCancelamento"
      @cancelar="paraCancelar = null"
    />
  </div>
</template>

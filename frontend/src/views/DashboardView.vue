<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, BarElement, CategoryScale, LinearScale, Tooltip } from 'chart.js'
import { indicadoresApi, especiesApi, mensagemDeErro } from '@/services/api'
import { numero, moeda, moedaCompacta } from '@/utils/formato'

ChartJS.register(BarElement, CategoryScale, LinearScale, Tooltip)

const indicadores = ref(null)
const especies = ref([])
const especieFiltro = ref('')
const carregando = ref(true)
const erro = ref('')

const CORES = { Sementes: '#4a8c64', Mudas: '#e07b3a', 'Óleo': '#e07b3a', Frutos: '#7ab896' }
const corDe = (tipo) => CORES[tipo] || '#2d5a40'

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    indicadores.value = await indicadoresApi.buscar(especieFiltro.value)
  } catch (e) {
    erro.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  especiesApi.listar().then(l => (especies.value = l)).catch(() => {})
  await carregar()
})
watch(especieFiltro, carregar)

const semDados = computed(() => indicadores.value && indicadores.value.estoquePorEspecie.length === 0)

const dadosGrafico = computed(() => {
  const itens = indicadores.value?.estoquePorEspecie ?? []
  return {
    labels: itens.map(i => `${i.especie} (${i.unidade})`),
    datasets: [{
      data: itens.map(i => Number(i.saldo)),
      backgroundColor: itens.map(i => corDe(i.tipoRecurso)),
      borderRadius: 6,
      maxBarThickness: 56,
    }],
  }
})

const opcoesGrafico = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: { callbacks: { label: (c) => ` ${numero(c.raw)}` } },
  },
  scales: {
    x: { grid: { display: false }, ticks: { color: '#7a9b8a', font: { family: 'Sora', size: 11 } } },
    y: { beginAtZero: true, grid: { color: '#eef2f0' }, ticks: { color: '#9ab8a8', font: { family: 'JetBrains Mono', size: 10 } } },
  },
}
</script>

<template>
  <div>
    <div class="filtro">
      <label for="filtro-especie">Filtrar por espécie</label>
      <select id="filtro-especie" v-model="especieFiltro" class="ui-select">
        <option value="">Todas as espécies</option>
        <option v-for="e in especies" :key="e.id" :value="e.id">{{ e.nomePopular }}</option>
      </select>
    </div>

    <div v-if="erro" class="aviso-inline">⚠️ {{ erro }}</div>
    <div v-else-if="carregando && !indicadores" class="carregando">Carregando indicadores…</div>

    <template v-else-if="indicadores">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-top"><div class="kpi-icon green">🌲</div></div>
          <div class="kpi-value mono">{{ numero(indicadores.matrizesMapeadas) }}</div>
          <div class="kpi-label">Matrizes Mapeadas</div>
          <div class="kpi-desc">Árvores sementeiras sincronizadas do app</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-top"><div class="kpi-icon green">🌾</div></div>
          <div class="kpi-value mono">{{ numero(indicadores.sacasEmEstoque) }}</div>
          <div class="kpi-label">Estoque de Sementes</div>
          <div class="kpi-desc">Sacas prontas para escoamento</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-top"><div class="kpi-icon green">🪴</div></div>
          <div class="kpi-value mono">{{ numero(indicadores.mudasNoViveiro) }}</div>
          <div class="kpi-label">Mudas no Viveiro</div>
          <div class="kpi-desc">Unidades disponíveis em estoque</div>
        </div>
        <div class="kpi-card accent-card">
          <div class="kpi-top"><div class="kpi-icon orange">💵</div></div>
          <div class="kpi-value mono" style="color:var(--earth-mid)">{{ moedaCompacta(indicadores.faturamentoEstimado) }}</div>
          <div class="kpi-label">Faturamento Estimado</div>
          <div class="kpi-desc">
            Inventário × valor de mercado · vendido no mês: {{ moeda(indicadores.faturamentoMes) }}
          </div>
        </div>
      </div>

      <div class="two-col">
        <div class="card">
          <div class="card-header">
            <div class="card-title">
              <div class="card-title-icon">📊</div>
              Estoque por Espécie
            </div>
            <span style="font-size:11px;color:var(--text-3);">Saldo atual</span>
          </div>
          <div v-if="semDados" class="vazio">
            Nenhum estoque registrado{{ especieFiltro ? ' para esta espécie' : '' }}.<br>
            Lance entradas na aba <strong>Estoque de Recursos</strong> ou sincronize com o aplicativo de campo.
          </div>
          <div v-else class="chart-wrap">
            <div class="chart-box">
              <Bar :data="dadosGrafico" :options="opcoesGrafico" />
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title">
              <div class="card-title-icon">🔔</div>
              Indicadores de Mercado
            </div>
          </div>
          <div class="card-body">
            <div v-if="indicadores.alertas.length === 0" class="vazio">Sem indicadores no momento.</div>
            <div v-else class="alert-list">
              <div v-for="(a, i) in indicadores.alertas" :key="i" class="alert-item" :class="a.tipo">
                <div class="alert-item-icon">{{ a.tipo === 'warn' ? '⚡' : '✅' }}</div>
                <div class="alert-item-text">
                  <h4>{{ a.titulo }}</h4>
                  <p>{{ a.mensagem }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="kpi-grid" style="grid-template-columns: repeat(2, 1fr);">
        <div class="kpi-card accent-card">
          <div class="kpi-value mono" style="color:var(--earth-mid)">{{ moeda(indicadores.faturamentoRealizado) }}</div>
          <div class="kpi-label">Faturamento Realizado</div>
          <div class="kpi-desc">Total de vendas concluídas no Livro de Vendas</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-value mono">{{ numero(indicadores.vendasMes) }}</div>
          <div class="kpi-label">Vendas no Mês</div>
          <div class="kpi-desc">Transações concluídas no mês atual</div>
        </div>
      </div>
    </template>
  </div>
</template>

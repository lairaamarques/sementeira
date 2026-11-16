<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { syncApi } from '@/services/api'
import { numero } from '@/utils/formato'

const status = ref(null)
const offline = ref(false)
const agora = ref(Date.now())
let timer

async function atualizar() {
  try {
    status.value = await syncApi.status()
    offline.value = false
  } catch {
    offline.value = true
  }
  agora.value = Date.now()
}

onMounted(() => {
  atualizar()
  timer = setInterval(atualizar, 60_000)
})
onUnmounted(() => clearInterval(timer))

function tempoDesde(iso) {
  const min = Math.floor((agora.value - new Date(iso).getTime()) / 60_000)
  if (min < 1) return 'agora mesmo'
  if (min < 60) return `há ${min} min`
  const h = Math.floor(min / 60)
  if (h < 24) return `há ${h} h`
  const d = Math.floor(h / 24)
  return `há ${d} dia${d > 1 ? 's' : ''}`
}

const estado = computed(() => {
  if (offline.value) return { classe: 'erro', texto: 'Servidor indisponível' }
  if (!status.value) return { classe: 'pendente', texto: 'Verificando sincronização…' }
  if (!status.value.ultimaSincronizacao) return { classe: 'pendente', texto: 'Aguardando 1ª sincronização do app' }
  return { classe: '', texto: `App Android sincronizado ${tempoDesde(status.value.ultimaSincronizacao)}` }
})

const detalhe = computed(() => {
  const s = status.value
  if (!s?.ultimaSincronizacao) return 'O app EcoSemente ainda não enviou matrizes para o servidor.'
  const quando = new Date(s.ultimaSincronizacao).toLocaleString('pt-BR')
  return `Último envio: ${quando}${s.dispositivo ? ` (${s.dispositivo})` : ''} · ${numero(s.totalMatrizes)} matrizes no servidor`
})
</script>

<template>
  <div class="sync-chip" :class="estado.classe" :title="detalhe">
    <div class="sync-dot"></div>
    {{ estado.texto }}
  </div>
</template>

<style scoped>
.sync-chip.pendente { background: #fef9c3; border-color: #fde68a; color: #a16207; }
.sync-chip.pendente .sync-dot { background: #eab308; }
.sync-chip.erro { background: #fff1f2; border-color: #fecdd3; color: #be123c; }
.sync-chip.erro .sync-dot { background: #e11d48; animation: none; }
</style>

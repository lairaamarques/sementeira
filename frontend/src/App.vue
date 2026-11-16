<script setup>
import { ref, computed } from 'vue'
import DashboardView from './views/DashboardView.vue'
import CatalogoView from './views/CatalogoView.vue'
import EstoqueView from './views/EstoqueView.vue'
import VendasView from './views/VendasView.vue'
import { toasts } from './services/toast'
import SyncChip from './components/SyncChip.vue'

const abas = {
  dashboard: { menu: 'Painel de Negócios', icone: '📊', title: 'Painel de Inteligência de Negócios', sub: 'Visão geral de KPIs e métricas operacionais', componente: DashboardView },
  catalogo: { menu: 'Catálogo de Espécies', icone: '🌱', title: 'Catálogo de Espécies Nativas', sub: 'Registro e gestão de variedades botânicas', componente: CatalogoView },
  estoque: { menu: 'Estoque de Recursos', icone: '📦', title: 'Movimentação de Estoque', sub: 'Controle de insumos e inventário', componente: EstoqueView },
  vendas: { menu: 'Livro de Vendas', icone: '💰', title: 'Livro de Vendas', sub: 'Registro e histórico de comercialização', componente: VendasView },
}

const viewAtual = ref('dashboard')
const abaAtual = computed(() => abas[viewAtual.value])
</script>

<template>
  <div class="app">
    <aside class="sidebar">
      <div class="sidebar-top">
        <div class="brand">
          <div class="brand-icon">🌳</div>
          <div class="brand-text">
            <div class="brand-name">EcoSemente BI</div>
            <div class="brand-sub">Dashboard</div>
          </div>
        </div>
        <div class="nav-label">Módulos</div>
        <ul class="nav-list">
          <li v-for="(aba, id) in abas" :key="id" class="nav-item" :class="{ active: viewAtual === id }">
            <a href="#" @click.prevent="viewAtual = id"><span class="nav-icon">{{ aba.icone }}</span> {{ aba.menu }}</a>
          </li>
        </ul>
      </div>

      <div class="sidebar-footer">
        <div class="user-row">
          <div class="user-ava">LL</div>
          <div>
            <div class="user-info-name">Laira Lima</div>
            <div class="user-info-sub">Gestora de Operações</div>
          </div>
        </div>
        <div class="tcc-badge">
          TCC I · IFAM – CA-TADS<br>
          Tecnologia Social Aplicada
        </div>
      </div>
    </aside>

    <div class="main">
      <header class="header">
        <div class="header-left">
          <h1>{{ abaAtual.title }}</h1>
          <p>{{ abaAtual.sub }}</p>
        </div>
        <div class="header-right">
          <SyncChip />
          <div style="width:1px;height:32px;background:var(--border);"></div>
          <div style="font-size:12px;color:var(--text-3);">Assoc. Produtores Ribeirinha &mdash; Manaus, AM</div>
        </div>
      </header>

      <div class="content-body">
        <component :is="abaAtual.componente" :key="viewAtual" class="view-panel active" />
      </div>
    </div>

    <div class="toasts" aria-live="polite">
      <div v-for="t in toasts" :key="t.id" class="toast" :class="t.tipo">{{ t.mensagem }}</div>
    </div>
  </div>
</template>

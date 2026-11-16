import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081/api',
})

export function mensagemDeErro(error) {
  if (error.response?.data?.mensagem) return error.response.data.mensagem
  if (error.request && !error.response) return 'Não foi possível falar com o servidor. O backend está rodando?'
  return 'Erro inesperado. Tente novamente.'
}

export const especiesApi = {
  listar: () => api.get('/especies').then(r => r.data),
  cadastrar: (dados) => api.post('/especies', dados).then(r => r.data),
  atualizar: (id, dados) => api.put(`/especies/${id}`, dados).then(r => r.data),
  inativar: (id) => api.patch(`/especies/${id}/inativar`),
}

export const estoqueApi = {
  listar: () => api.get('/estoque').then(r => r.data),
  lancarEntrada: (dados) => api.post('/estoque', dados).then(r => r.data),
  desfazerEntrada: (id) => api.delete(`/estoque/${id}`),
  registrarSaida: (id, dados) => api.post(`/estoque/${id}/saidas`, dados).then(r => r.data),
  listarSaidas: () => api.get('/estoque/saidas').then(r => r.data),
  desfazerSaida: (movId) => api.delete(`/estoque/saidas/${movId}`),
  listarFamilias: () => api.get('/estoque/familias').then(r => r.data),
}

export const vendasApi = {
  listar: () => api.get('/vendas').then(r => r.data),
  registrar: (dados) => api.post('/vendas', dados).then(r => r.data),
  cancelar: (id) => api.patch(`/vendas/${id}/cancelar`).then(r => r.data),
}

export const matrizesApi = {
  listar: () => api.get('/semente').then(r => r.data),
}

export const syncApi = {
  status: () => api.get('/sync/status').then(r => r.data),
}

export const indicadoresApi = {
  buscar: (especieId) => api.get('/indicadores', { params: { especieId: especieId || undefined } }).then(r => r.data),
}

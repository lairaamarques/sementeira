const numeroFmt = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 })
const moedaFmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dataFmt = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })

export const numero = (n) => numeroFmt.format(Number(n ?? 0))

export const moeda = (n) => moedaFmt.format(Number(n ?? 0))

export function moedaCompacta(n) {
  const v = Number(n ?? 0)
  if (Math.abs(v) < 10000) return moeda(v)
  return 'R$ ' + new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 }).format(v)
}

export const data = (iso) => (iso ? dataFmt.format(new Date(iso)) : '—')

export const nomeProduto = (especie) => `${especie.tipoRecurso} de ${especie.nomePopular}`

export const rotuloLote = (lote) =>
  `${nomeProduto(lote.especie)} · ${lote.familiaProdutora.nome} (${numero(lote.saldo)} ${lote.unidade})`

import { reactive } from 'vue'

export const toasts = reactive([])

let proximoId = 1

export function mostrarToast(mensagem, tipo = 'sucesso') {
  const id = proximoId++
  toasts.push({ id, mensagem, tipo })
  setTimeout(() => {
    const i = toasts.findIndex(t => t.id === id)
    if (i !== -1) toasts.splice(i, 1)
  }, tipo === 'erro' ? 5000 : 3000)
}

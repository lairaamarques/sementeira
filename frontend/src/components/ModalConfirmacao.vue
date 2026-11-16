<script setup>
defineProps({
  titulo: { type: String, required: true },
  mensagem: { type: String, required: true },
  textoConfirmar: { type: String, default: 'Confirmar' },
})
const emit = defineEmits(['confirmar', 'cancelar'])
</script>

<template>
  <div class="modal-fundo" @click.self="emit('cancelar')" @keydown.esc="emit('cancelar')">
    <div class="modal" role="dialog" aria-modal="true">
      <h3>{{ titulo }}</h3>
      <p>{{ mensagem }}</p>
      <div class="form-footer">
        <button type="button" class="btn btn-ghost" @click="emit('cancelar')">Cancelar</button>
        <button type="button" class="btn btn-accent" @click="emit('confirmar')">{{ textoConfirmar }}</button>
      </div>
    </div>
  </div>
</template>

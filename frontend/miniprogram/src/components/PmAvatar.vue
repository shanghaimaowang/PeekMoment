<template>
<image v-if="src" class="user-photo" :class="{large: large}" :src="src" mode="aspectFill" />
<text v-else class="user-photo fallback-photo" :class="{large: large}">{{ name.slice(0,1) }}</text>
</template>
<script>
const pictures = { yuan: '/static/avatar-cat.png', lin: '/static/avatar-orange.png', demo: '/static/avatar-tree.png' }
export default {
  options: {styleIsolation: 'shared', virtualHost: true},
  props: {userId: String, name: {type: String, default: ''}, large: Boolean},
  computed: { src() { return pictures[this.userId] || '' } }
}
</script>

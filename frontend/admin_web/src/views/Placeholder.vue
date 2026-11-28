<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '页面')
</script>

<template>
  <el-card shadow="never">
    <template #header>{{ title }}</template>
    <el-empty :description="`${title} 开发中`" />
  </el-card>
</template>

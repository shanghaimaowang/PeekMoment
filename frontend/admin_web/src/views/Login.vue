<script setup>
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

function handleLogin() {
  // 假登录：先写个 token，后续换成真实接口
  localStorage.setItem('admin_token', 'fake-token')
  router.replace(route.query.redirect || '/home')
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">PeekMoment 管理后台</div>
      </template>

      <el-button type="primary" size="large" class="login-btn" @click="handleLogin">
        登录
      </el-button>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: grid;
  place-items: center;
  background: #f5f7fa;
}
.login-card {
  width: 360px;
}
.login-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}
.login-btn {
  width: 100%;
}
</style>

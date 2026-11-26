<template>
  <view class="page">
    <pm-header title="联调广场"/>
    <view v-if="enabled" class="pad">
      <view class="form-panel">
        <text class="field-label">发布到开发服务器</text>
        <input v-model="token" password class="comment-input" placeholder="开发环境发布令牌"/>
        <button class="primary wide" :disabled="!token.trim()" @click="compose">写动态</button>
      </view>
      <view class="section-head">
        <text class="section-title">服务器动态</text>
        <button class="link" :disabled="loading" @click="refresh">刷新</button>
      </view>
      <pm-card v-for="item in items" :key="item.id" :moment="item" read-only/>
      <view v-if="error" class="empty">
        <text class="body-small">{{ error }}</text>
        <button class="link" @click="load">重试</button>
      </view>
      <text v-else-if="loading" class="muted">正在读取…</text>
      <text v-else-if="!items.length" class="muted">服务器上还没有动态。</text>
      <button v-else-if="more" class="secondary wide" @click="load">加载更多</button>
    </view>
    <view v-else class="empty"><text class="muted">请在开发构建中打开。</text></view>
  </view>
</template>
<script>
import PmCard from '../../components/PmCard.vue'
import PmHeader from '../../components/PmHeader.vue'
import { apiOrigin, listMoments, setPublisherToken } from '../../services/development.js'

export default {
  components: { PmCard, PmHeader },
  data() {
    return { enabled: !!apiOrigin, token: '', items: [], page: 1, more: false,
      loading: false, error: '', generation: 0 }
  },
  onShow() { if (this.enabled) this.refresh() },
  onUnload() { this.generation++; setPublisherToken() },
  methods: {
    compose() {
      setPublisherToken(this.token)
      uni.navigateTo({url:'/pages/compose/index?source=server'})
    },
    refresh() {
      this.generation++
      this.page = 1
      this.loading = false
      this.load()
    },
    async load() {
      if (this.loading) return
      const generation = this.generation, page = this.page
      this.loading = true
      this.error = ''
      try {
        const result = await listMoments(page)
        if (generation !== this.generation) return
        const previous = page === 1 ? [] : this.items
        const seen = new Set(previous.map(item => item.id))
        this.items = [...previous, ...result.items.filter(item => !seen.has(item.id))]
        this.more = page * result.pageSize < result.total
        this.page++
      } catch (error) {
        if (generation === this.generation) this.error = error.message
      } finally {
        if (generation === this.generation) this.loading = false
      }
    }
  }
}
</script>

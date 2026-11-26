<template>
  <view>
    <view class="section-head">
      <text class="section-title">图片或视频</text>
      <text class="muted">最多 9 张图或 1 段视频</text>
    </view>
    <view v-if="items.length" class="attachments">
      <view v-for="(item,i) in items" :key="item.src+i" class="attachment">
        <pm-media :item="item"/>
        <text v-if="item.state" class="transfer-state">{{ item.state }}</text>
        <button class="remove-media" aria-label="移除媒体" :disabled="disabled || choosing" @click="$emit('remove', i)">×</button>
      </view>
    </view>
    <view class="pick-actions">
      <button v-if="canImage" class="pick-button" :disabled="disabled || choosing" @click="pick('image')">
        <image class="icon" src="/static/image-ink.png"/>选照片
      </button>
      <button v-if="!items.length" class="pick-button" :disabled="disabled || choosing" @click="pick('video')">
        <image class="icon" src="/static/video-ink.png"/>选视频
      </button>
    </view>
  </view>
</template>
<script>
import PmMedia from './PmMedia.vue'
import { chooseImages } from '../services/upload.js'
import { problem } from '../services/nav.js'

export default {
  options: { styleIsolation: 'shared', virtualHost: true },
  components: { PmMedia },
  props: { items: { type: Array, required: true }, disabled: Boolean },
  emits: ['add', 'remove', 'choosing'],
  data() { return { choosing: false } },
  computed: {
    canImage() { return this.items.length < 9 && this.items.every(item => item.kind === 'image') }
  },
  methods: {
    async pick(kind) {
      if (this.disabled || this.choosing) return
      this.choosing = true
      this.$emit('choosing', true)
      try {
        const paths = kind === 'image' ? await chooseImages(9 - this.items.length) :
          await new Promise((resolve, reject) => uni.chooseVideo({
            sourceType: ['album', 'camera'], compressed: true, maxDuration: 60,
            success: result => resolve([result.tempFilePath]), fail: reject
          }))
        this.$emit('add', paths.map(src => ({ kind, src, fresh: true })))
      } catch (error) {
        if (!String(error?.errMsg || error).toLowerCase().includes('cancel')) problem(error)
      } finally {
        this.choosing = false
        this.$emit('choosing', false)
      }
    }
  }
}
</script>
<style scoped>
.transfer-state { position: absolute; bottom: 4px; left: 4px; color: white; background: rgba(0,0,0,.64); padding: 2px 5px; font-size: 11px; }
</style>

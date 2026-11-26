<template>
<view class="page">
  <pm-header :title="id?'编辑动态':'发布动态'"/>
  <view v-if="ready" class="pad compose">
    <view class="form-panel">
      <input v-model="title" class="title-input" placeholder="标题（选填）" maxlength="40" :disabled="busy"/>
      <view class="rule"></view>
      <view class="field-head">
        <text class="field-label">正文</text>
        <text class="micro-inline">{{ body.length }}/1000</text>
      </view>
      <textarea v-model="body" class="body-input" placeholder="有什么想说的？" maxlength="1000" :disabled="busy"/>
    </view>
    <view class="section-head">
      <text class="section-title">图片或视频</text>
      <text class="muted">最多 9 张图或 1 段视频</text>
    </view>
    <view v-if="media.length" class="attachments">
      <view v-for="(m,i) in media" :key="m.src" class="attachment">
        <pm-media :item="m"/>
        <button class="remove-media" aria-label="移除媒体" :disabled="busy" @click="drop(i)">×</button>
      </view>
    </view>
    <view class="pick-actions">
      <button v-if="canImage" class="pick-button" :disabled="busy" @click="pickImages">
        <image class="icon" src="/static/image-ink.png"/>
        选照片
      </button>
      <button v-if="!media.length" class="pick-button" :disabled="busy" @click="pickVideo">
        <image class="icon" src="/static/video-ink.png"/>
        选视频
      </button>
    </view>
    <view class="section-head">
      <text class="section-title">标签</text>
      <text class="muted">{{ tags.length }}/5</text>
    </view>
    <view class="chips wrap">
      <button v-for="t in choices" :key="t" class="chip" :class="{selected:tags.includes(t)}" :disabled="busy" @click="toggle(t)"># {{ t }}</button>
    </view>
    <view class="custom-tag">
      <input v-model="custom" placeholder="自定义标签" maxlength="12" :disabled="busy" @confirm="addTag"/>
      <button class="link" :disabled="busy" @click="addTag">添加</button>
    </view>
    <button class="primary wide submit" :disabled="busy" @click="submit">{{ busy?'正在保存…':(id?'保存修改':'发布') }}</button>
  </view>
  <view v-else class="empty">
    <text class="section-title">无法编辑这条动态</text>
    <button class="link" @click="returnBack">返回</button>
  </view>
</view>
</template>
<script>
import PmHeader from '../../components/PmHeader.vue'
import PmMedia from '../../components/PmMedia.vue'
import { user, post, validate, savePost } from '../../services/store.js'
import { topics } from '../../services/seed.js'
import { persist, cleanup } from '../../services/media.js'
import { back, toast, problem } from '../../services/nav.js'

export default {
  components: { PmHeader, PmMedia },
  data() {
    return { id: '', ready: false, title: '', body: '', tags: [], custom: '', media: [], original: [], busy: false }
  },
  onLoad({ id = '' }) {
    this.id = id
    const current = user()
    if (!current) return
    if (id) {
      const saved = post(id)
      if (!saved || saved.userId !== current.id) return
      this.title = saved.title
      this.body = saved.body
      this.tags = [...saved.tags]
      this.original = saved.media
      this.media = saved.media.map(file => ({ ...file }))
    }
    this.ready = true
  },
  computed: {
    choices() { return [...new Set([...topics, ...this.tags])] },
    canImage() { return this.media.length < 9 && this.media.every(file => file.kind === 'image') }
  },
  methods: {
    returnBack: back,
    toggle(tag) {
      if (this.tags.includes(tag)) this.tags = this.tags.filter(item => item !== tag)
      else if (this.tags.length < 5) this.tags.push(tag)
      else toast('最多选择 5 个标签')
    },
    addTag() {
      const tag = this.custom.trim().replace(/^#+/, '').trim()
      if (!tag) return
      if (!this.tags.includes(tag)) this.toggle(tag)
      if (this.tags.includes(tag)) this.custom = ''
    },
    drop(index) { this.media.splice(index, 1) },
    pickError(error) {
      if (!String(error?.errMsg || error).toLowerCase().includes('cancel')) problem(error)
    },
    pickImages() {
      if (!this.canImage) return
      uni.chooseImage({
        count: 9 - this.media.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: result => {
          const chosen = result.tempFilePaths.slice(0, 9 - this.media.length)
          this.media.push(...chosen.map(src => ({ kind: 'image', src, fresh: true })))
        },
        fail: this.pickError
      })
    },
    pickVideo() {
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        compressed: true,
        maxDuration: 60,
        success: result => { this.media = [{ kind: 'video', src: result.tempFilePath, fresh: true }] },
        fail: this.pickError
      })
    },
    async submit() {
      if (this.busy) return
      this.busy = true
      const added = []
      const files = []
      let savedId
      try {
        const draft = { title: this.title, body: this.body, tags: this.tags, media: this.media }
        validate(draft)
        for (const media of this.media) {
          const src = media.fresh ? await persist(media.src) : media.src
          if (media.fresh) added.push(src)
          files.push({ kind: media.kind, src })
        }
        savedId = savePost(this.id, { ...draft, media: files })
      } catch (error) {
        await cleanup(added)
        this.busy = false
        problem(error)
        return
      }
      // 元数据写入成功后才清理旧文件，导航失败也不回滚已发布的媒体。
      const removed = this.original.filter(old => !files.some(file => file.src === old.src))
      await cleanup(removed.map(file => file.src))
      if (this.id && getCurrentPages().length > 1) back()
      else uni.redirectTo({ url: '/pages/detail/index?id=' + encodeURIComponent(savedId) })
    }
  }
}
</script>

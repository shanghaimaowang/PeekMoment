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
    <pm-attachment-picker v-if="!remote || remoteMedia" :items="media" :disabled="busy" @add="addMedia" @remove="drop" @choosing="choosing=$event"/>
    <text v-else class="muted">联调广场本周支持文字和标签发布。</text>
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
    <button class="primary wide submit" :disabled="busy || choosing" @click="submit">{{ busy?'正在保存…':(id?'保存修改':'发布') }}</button>
  </view>
  <view v-else class="empty">
    <text class="section-title">无法编辑这条动态</text>
    <button class="link" @click="returnBack">返回</button>
  </view>
</view>
</template>
<script>
import PmHeader from '../../components/PmHeader.vue'
import PmAttachmentPicker from '../../components/PmAttachmentPicker.vue'
import { user, post, validate, savePost } from '../../services/store.js'
import { topics } from '../../services/seed.js'
import { cleanup } from '../../services/media.js'
import { prepareAttachments } from '../../services/upload.js'
import { canPublish, publishMoment, transferToServer } from '../../services/development.js'
import { back, toast, problem } from '../../services/nav.js'

export default {
  components: { PmHeader, PmAttachmentPicker },
  data() {
    return { id: '', ready: false, title: '', body: '', tags: [], custom: '', media: [], original: [], busy: false, choosing: false, closed: false, remote: false, remoteMedia: !!transferToServer }
  },
  onLoad({ id = '', source = '' }) {
    if (source === 'server') {
      this.remote = true
      this.ready = canPublish()
      return
    }
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
  onUnload() {
    this.closed = true
    this.releaseDraft()
  },
  computed: {
    choices() { return [...new Set([...topics, ...this.tags])] }
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
    addMedia(items) { this.media.push(...items) },
    drop(index) {
      const [item] = this.media.splice(index, 1)
      if (item?.prepared && !this.remote) cleanup([item.prepared])
    },
    releaseDraft() {
      if (this.remote) return
      const paths = this.media.filter(item => item.prepared).map(item => {
        const src = item.prepared
        delete item.prepared
        return src
      })
      return cleanup(paths)
    },
    async submit() {
      if (this.busy || this.choosing || this.closed) return
      this.busy = true
      let savedId
      try {
        const draft = { title: this.title, body: this.body, tags: this.tags, media: this.media }
        validate(draft)
        const files = await prepareAttachments(this.media, {
          transfer: this.remote ? transferToServer : undefined,
          active: () => !this.closed,
          onState: (i, state) => { this.media[i].state = state },
          onPrepared: (i, src) => {
            if (!this.closed) this.media[i].prepared = src
            else if (!this.remote) cleanup([src])
          }
        })
        if (this.closed) return
        if (this.remote) {
          await publishMoment({ ...draft, media: files })
          if (!this.closed) back()
          return
        }
        savedId = savePost(this.id, { ...draft, media: files })
        // Once committed, these references belong to the post, even if navigation fails.
        for (const item of this.media) delete item.prepared
        const removed = this.original.filter(old => !files.some(file => file.src === old.src))
        await cleanup(removed.map(file => file.src))
      } catch (error) {
        this.busy = false
        if (!this.closed) problem(error)
        return
      }
      if (this.closed) return
      if (this.id && getCurrentPages().length > 1) back()
      else uni.redirectTo({ url: '/pages/detail/index?id=' + encodeURIComponent(savedId) })
    }
  }
}
</script>

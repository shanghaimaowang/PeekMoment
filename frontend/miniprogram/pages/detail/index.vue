<template>
<view class="page">
  <pm-header title="动态详情"/>
  <view v-if="p" class="pad detail-pad">
    <view class="detail-by">
      <pm-avatar :user-id="p.userId" :name="p.author" />
      <view>
        <text class="author">{{ p.author }}</text>
        <text class="micro">{{ dateText }}</text>
      </view>
    </view>
    <text v-if="p.title" class="detail-title">{{ p.title }}</text>
    <text v-if="p.body" class="body-copy">{{ p.body }}</text>
    <view v-if="p.media.length" class="gallery" :class="{multi:p.media.length>1}">
      <view v-for="m in p.media" :key="m.src" class="gallery-item">
        <pm-media :item="m" :gallery="p.media" playable/>
      </view>
    </view>
    <view class="card-tags">
      <text v-for="tag in p.tags" :key="tag" class="small-tag"># {{ tag }}</text>
    </view>
    <view v-if="owned" class="manage">
      <button class="link" @click="edit">编辑动态</button>
      <button class="link danger" @click="remove">删除动态</button>
    </view>
    <view class="rating-panel">
      <view class="rating-line">
        <text class="section-title">评分</text>
        <text class="score">
          {{ average }}
          <text class="micro-inline">/ 5</text>
        </text>
      </view>
      <view class="rating-line">
        <view class="stars">
          <button v-for="n in [1,2,3,4,5]" :key="n" class="star-btn" :class="{lit:n<=ownScore}" :aria-label="n+'星'" @click="vote(n)">
            {{ n<=ownScore?'★':'☆' }}
          </button>
        </view>
        <text class="muted">{{ ownScore ? '我的评分' : '点星评分' }}</text>
      </view>
    </view>
    <view id="comments" class="section-head">
      <text class="section-title">评论</text>
      <text class="muted">{{ p.comments.length }} 条</text>
    </view>
    <view v-for="c in p.comments" :key="c.id" class="comment-row">
      <pm-avatar :user-id="c.userId" :name="c.author" />
      <view class="comment-copy">
        <text class="author">{{ c.author }}</text>
        <text class="comment-body">{{ c.body }}</text>
      </view>
    </view>
    <text v-if="!p.comments.length" class="muted no-comments">还没有评论，聊聊你的感受吧。</text>
    <view class="comment-form">
      <input v-model="message" class="comment-input" maxlength="200" placeholder="写下你的评论…" @confirm="send"/>
      <button class="primary slim" @click="send">发送</button>
    </view>
  </view>
  <view v-else class="empty">
    <text class="section-title">这条片刻已不存在</text>
    <button class="link" @click="homeFeed">返回广场</button>
  </view>
</view>
</template>
<script>
import PmAvatar from '../../components/PmAvatar.vue'
import PmHeader from '../../components/PmHeader.vue'
import PmMedia from '../../components/PmMedia.vue'
import { post, user, localDate, clock, rating, rate, comment, removePost } from '../../services/store.js'
import { cleanup } from '../../services/media.js'
import { open, back, home, toast, problem, confirmAction } from '../../services/nav.js'

export default {
  components: { PmHeader, PmMedia, PmAvatar },
  data() { return { id: '', p: null, me: null, message: '', jump: false } },
  onLoad(query) {
    this.id = query.id || ''
    this.jump = query.section === 'comments'
  },
  onShow() { this.refresh() },
  onReady() {
    if (this.jump) uni.pageScrollTo({ selector: '#comments', duration: 150 })
  },
  computed: {
    owned() { return this.me && this.me.id === this.p?.userId },
    ownScore() { return this.p?.ratings[this.me?.id] || 0 },
    average() { return this.p ? rating(this.p) : '—' },
    dateText() { return localDate(this.p.createdAt) + ' · ' + clock(this.p.createdAt) }
  },
  methods: {
    refresh() { this.p = post(this.id); this.me = user() },
    homeFeed() { home('feed') },
    edit() { open('compose', this.id) },
    async remove() {
      const confirmed = await confirmAction('删除这条片刻？', '删除后，图片、评分和评论将一起移除。')
      if (!confirmed) return
      try {
        const removed = removePost(this.id)
        await cleanup(removed.map(file => file.src))
        back()
      } catch (error) { problem(error) }
    },
    vote(value) {
      if (!this.me) return open('login')
      try {
        rate(this.id, value)
        this.refresh()
        toast('已评 ' + value + ' 星')
      } catch (error) { problem(error) }
    },
    send() {
      if (!this.me) return open('login')
      try {
        comment(this.id, this.message)
        this.message = ''
        this.refresh()
        toast('评论已发送')
      } catch (error) { problem(error) }
    }
  }
}
</script>

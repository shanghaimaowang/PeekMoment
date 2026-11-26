<template>
<view class="entry">
  <view class="entry-head" @click="detail">
    <pm-avatar :user-id="moment.userId" :name="moment.author" />
    <view class="who">
      <text class="author">{{ moment.author }}</text>
      <text class="micro">{{ time }}</text>
    </view>
  </view>
  <view class="entry-copy" @click="detail">
    <text v-if="moment.title" class="entry-title">{{ moment.title }}</text>
    <text v-if="moment.body" class="entry-text">{{ moment.body }}</text>
  </view>
  <view v-if="moment.media.length" class="stream-media" :class="{tiles: moment.media.length>1}">
    <view v-for="m in moment.media" :key="m.src" class="stream-tile" @click="openVideo(m)">
      <pm-media :item="m" :gallery="moment.media" :playable="readOnly" />
    </view>
  </view>
  <view class="entry-bottom">
    <view class="inline-tags">
      <view v-for="t in moment.tags" :key="t">
        <text v-if="readOnly" class="topic-link">#{{ t }}</text>
        <button v-else class="topic-link" @click="tag(t)">#{{ t }}</button>
      </view>
    </view>
    <view v-if="!readOnly" class="metrics">
      <button class="metric" :aria-label="'查看评分 '+score" @click="detail">
        <image src="/static/star-ink.png"/>
        <text>{{ score==='—'?'评分':score }}</text>
      </button>
      <button class="metric" aria-label="查看评论" @click="comments">
        <image src="/static/comment-ink.png"/>
        <text>{{ moment.comments.length || '评论' }}</text>
      </button>
    </view>
  </view>
</view>
</template>
<script>
import PmAvatar from './PmAvatar.vue'
import PmMedia from './PmMedia.vue'
import {dateLabel, clock, rating} from '../services/store.js'
import {open} from '../services/nav.js'

export default {
  options: {styleIsolation: 'shared', virtualHost: true},
  components: {PmAvatar, PmMedia},
  props: {moment: Object, readOnly: Boolean},
  computed: {
    time() { return dateLabel(this.moment.createdAt) + ' ' + clock(this.moment.createdAt) },
    score() { return rating(this.moment) }
  },
  methods: {
    detail() { if (!this.readOnly) open('detail', this.moment.id) },
    comments() { uni.navigateTo({url: '/pages/detail/index?id=' + encodeURIComponent(this.moment.id) + '&section=comments'}) },
    tag(t) { uni.navigateTo({url: '/pages/search/index?topic=' + encodeURIComponent(t)}) },
    openVideo(m) { if (m.kind === 'video') this.detail() }
  }
}
</script>

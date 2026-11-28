<template>
<view class="topbar" :style="barStyle">
  <view v-if="brand" class="brand">
    <image v-if="!title" class="logo" src="/static/logo-green.png"/>
    <text>{{ title || '片刻' }}</text>
  </view>
  <view v-else class="header-left">
    <button class="icon-btn" aria-label="返回" @click="back">
      <image class="icon" src="/static/back-ink.png"/>
    </button>
    <text class="header-title">{{ title }}</text>
  </view>
  <button v-if="search" class="icon-btn search-top" aria-label="搜索片刻" @click="openSearch">
    <image class="icon" src="/static/search-ink.png"/>
  </button>
</view>
</template>
<script>
import {back, open} from '../services/nav.js'
export default {
 options: {styleIsolation: 'shared', virtualHost: true},
  props: {brand: Boolean, title: String, search: Boolean},
  data() { return {barStyle:{}} },
  created() {
    // #ifdef MP-WEIXIN
    const b = uni.getMenuButtonBoundingClientRect()
    const s = uni.getSystemInfoSync()
    this.barStyle = {paddingTop: b.top+'px', paddingBottom: '8px', minHeight: b.height+'px', paddingRight: (s.windowWidth-b.left+8)+'px'}
    // #endif
  },
  methods: { back, openSearch(){ open('search') } }
}
</script>

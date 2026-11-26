<template>
<view class="tab-wrap">
  <view class="tabbar">
    <button class="tab-item" :class="{active: current==='feed'}" @click="tab('feed')">
      <image class="tab-ico" :src="current==='feed' ? '/static/feed-green.png' : '/static/feed-ink.png'"/>
      <text>广场</text>
    </button>
    <button class="publish-tab" @click="compose">
      <image class="tab-ico" src="/static/plus-white.png"/>
      发布
    </button>
    <button class="tab-item" :class="{active: current==='profile'}" @click="tab('profile')">
      <image class="tab-ico" :src="current==='profile' ? '/static/person-green.png' : '/static/person-ink.png'"/>
      <text>我的</text>
    </button>
  </view>
</view>
</template>
<script>
import {editor, home} from '../services/nav.js'
import {user} from '../services/store.js'
export default {
 options: {styleIsolation: 'shared', virtualHost: true},props:{current:String},methods:{ tab(p){if(p!==this.current) home(p)}, compose(){editor(user())} }}
</script>

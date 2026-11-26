<template>
<view class="media-host">
  <video v-if="url && item.kind==='video' && playable" class="media-frame" :src="url" controls object-fit="contain"/>
  <view v-else-if="item.kind==='video'" class="video-placeholder">
    <image class="medium-icon" src="/static/video-ink.png"/>
    <text>视频片刻</text>
  </view>
  <image v-else-if="url && !failed" :src="url" class="media-frame" mode="aspectFill" @error="failed=true" @click="zoom"/>
  <text v-else class="media-hint">{{ failed ? '媒体暂时无法显示' : '正在载入…' }}</text>
</view>
</template>
<script>
import {resolveMedia} from '../services/media.js'
import {problem} from '../services/nav.js'
export default {
 options: {styleIsolation: 'shared', virtualHost: true},
 props:{item:Object, gallery:Array, playable:Boolean},
 data(){return {url:'', failed:false}},
 watch:{'item.src': {immediate:true, async handler(src){this.failed=false;this.url='';try {
   const url=await resolveMedia(src); if(this.item.src===src) this.url=url
 } catch { if(this.item.src===src) this.failed=true }}}},
 methods:{async zoom(){
   if(!this.gallery) return
   try {const urls=await Promise.all(this.gallery.filter(m=>m.kind==='image').map(m=>resolveMedia(m.src)));uni.previewImage({current:this.url,urls})}catch(e){problem(e)}
 }}
}
</script>

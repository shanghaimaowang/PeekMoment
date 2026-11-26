<template>
<view class="page with-tabs">
  <pm-header brand search/>
  <view class="pad">
    <view class="feed-tabs">
      <button v-for="t in choices" :key="t" class="feed-filter" :class="{selected:topic===t}" @click="select(t)">{{ t }}</button>
    </view>
    <pm-card v-for="p in visible" :key="p.id" :moment="p"/>
    <view v-if="!filtered.length" class="empty">
      <text class="section-title">这个话题还没有片刻</text>
      <text class="muted">换个话题，或发布第一条动态。</text>
    </view>
    <button v-if="visible.length < filtered.length" class="secondary wide" @click="more">加载更多</button>
  </view>
  <pm-nav current="feed"/>
</view>
</template>
<script>
import PmHeader from '../../components/PmHeader.vue'
import PmNav from '../../components/PmNav.vue'
import PmCard from '../../components/PmCard.vue'
import {list} from '../../services/store.js'
import {topics} from '../../services/seed.js'
export default {components:{PmHeader,PmNav,PmCard},data(){return {choices:['全部',...topics],topic:'全部',filtered:[],limit:10}},
 computed:{visible(){return this.filtered.slice(0,this.limit)}},onShow(){this.refresh()},onReachBottom(){this.more()},
 methods:{refresh(){this.filtered=list({topic:this.topic==='全部'?'':this.topic})},select(t){this.topic=t;this.limit=10;this.refresh()},more(){this.limit+=10}}
}
</script>

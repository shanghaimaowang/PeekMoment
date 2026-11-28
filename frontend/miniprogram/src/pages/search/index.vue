<template>
<view class="page">
  <pm-header title="搜索片刻"/>
  <view class="pad">
    <view class="search-field search-start">
      <image class="icon" src="/static/search-ink.png"/>
      <input v-model="query" placeholder="搜标题、正文、作者或标签" maxlength="80" confirm-type="search"/>
      <button v-if="query" class="icon-btn small" aria-label="清除关键词" @click="query=''">
        <image class="mini-icon" src="/static/close-ink.png"/>
      </button>
    </view>
    <view class="chips wrap">
      <button v-for="t in choices" :key="t" class="chip" :class="{selected:topic===t}" @click="topic=t">{{ t }}</button>
    </view>
    <view class="date-line">
      <picker mode="date" :value="day" @change="setDay">
        <view class="date-pick">
          <image class="mini-icon" src="/static/calendar-ink.png"/>
          <text>{{ day || '选择日期' }}</text>
        </view>
      </picker>
      <button v-if="day" class="link" @click="day=''">清除日期</button>
    </view>
    <view class="section-head">
      <text class="section-title">搜索结果</text>
      <text class="muted">{{ results.length }} 条</text>
    </view>
    <pm-card v-for="p in results" :key="p.id" :moment="p"/>
    <view v-if="!results.length" class="empty">
      <text class="section-title">没有搜到相关片刻</text>
      <button class="link" @click="reset">清空筛选</button>
    </view>
  </view>
</view>
</template>
<script>
import PmHeader from '../../components/PmHeader.vue'
import PmCard from '../../components/PmCard.vue'
import {list,filterPosts} from '../../services/store.js'
import {topics} from '../../services/seed.js'
export default {components:{PmHeader,PmCard},data(){return {query:'',topic:'全部',day:'',choices:['全部',...topics],posts:[]}},
 onLoad(q){if(q.topic){this.topic=q.topic;if(!this.choices.includes(q.topic))this.choices.push(q.topic)}},
 onShow(){this.posts=list()},
 computed:{results(){return filterPosts(this.posts,{topic:this.topic==='全部'?'':this.topic,query:this.query,day:this.day})}},
 methods:{setDay(e){this.day=e.detail.value},reset(){this.day='';this.query='';this.topic='全部'}}
}
</script>

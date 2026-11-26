<template>
<view class="page with-tabs">
  <pm-header brand title="我的"/>
  <view class="pad">
    <view v-if="me" class="profile-card">
      <view class="profile-line">
        <pm-avatar :user-id="me.id" :name="me.name" large />
        <view class="grow">
          <text class="profile-name">{{ me.name }}</text>
          <text class="micro">本地演示账号</text>
        </view>
        <button class="link dim" @click="leave">退出登录</button>
      </view>
      <view class="profile-stats">
        <view>
          <text class="stat-n">{{ mine.length }}</text>
          <text>已发动态</text>
        </view>
        <view>
          <text class="stat-n">{{ commentCount }}</text>
          <text>收到评论</text>
        </view>
        <view>
          <text class="stat-n">{{ voteCount }}</text>
          <text>收到评分</text>
        </view>
      </view>
    </view>
    <view v-else class="form-panel">
      <text class="section-title">我的动态</text>
      <text class="body-small">登录后可以查看、编辑自己发布的动态。</text>
      <button class="primary wide" @click="enter">登录演示账号</button>
    </view>
    <view v-if="me">
      <view class="section-head">
        <text class="section-title">我的动态</text>
        <button class="link" @click="compose">发动态</button>
      </view>
      <pm-card v-for="p in mine" :key="p.id" :moment="p"/>
      <view v-if="!mine.length" class="empty">
        <text class="body-small">还没有发布动态。</text>
        <button class="link" @click="compose">发布第一条片刻</button>
      </view>
    </view>
  </view>
  <pm-nav current="profile"/>
</view>
</template>
<script>
import PmHeader from '../../components/PmHeader.vue'
import PmAvatar from '../../components/PmAvatar.vue'
import PmCard from '../../components/PmCard.vue'
import PmNav from '../../components/PmNav.vue'
import {user,list,logout} from '../../services/store.js'
import {open,confirmAction,problem} from '../../services/nav.js'
export default {components:{PmHeader,PmAvatar,PmCard,PmNav},data(){return {me:null,mine:[]}},onShow(){this.refresh()},
 computed:{commentCount(){return this.mine.reduce((n,p)=>n+p.comments.length,0)},voteCount(){return this.mine.reduce((n,p)=>n+Object.keys(p.ratings).length,0)}},
 methods:{refresh(){this.me=user();this.mine=list({mine:true})},enter(){open('login')},compose(){open('compose')},async leave(){if(await confirmAction('退出登录？','你发布的动态会保留在这台设备上。')){try{logout();this.refresh()}catch(e){problem(e)}}}}
}
</script>

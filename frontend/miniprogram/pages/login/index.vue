<template>
<view class="page">
  <pm-header title="登录"/>
  <view class="pad">
    <view class="form-panel">
      <text class="section-title">选择账号</text>
      <text class="body-small">用这个账号体验发布和评论，内容保存在本机。</text>
      <view class="account">
        <pm-avatar user-id="demo" name="小满" large />
        <view>
          <text class="author">小满</text>
          <text class="micro">本地演示用户</text>
        </view>
      </view>
      <button class="primary wide" @click="enter">使用演示账号登录</button>
    </view>
  </view>
</view>
</template>
<script>
import PmAvatar from '../../components/PmAvatar.vue'
import PmHeader from '../../components/PmHeader.vue'
import {login} from '../../services/store.js'
import {back,problem} from '../../services/nav.js'
export default {components:{PmHeader,PmAvatar},data(){return {next:''}},onLoad(q){this.next=q.next||''},
 methods:{enter(){try{login();if(this.next==='compose')uni.redirectTo({url:'/pages/compose/index'});else back()}catch(e){problem(e)}}}
}
</script>

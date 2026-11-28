<template>
  <el-container class="admin-layout">
    <el-header class="header" height="50px">
      PeekMoment管理系统
    </el-header>

    <el-container class="admin-body">
      <el-aside width="150px" class="aside">
        <el-menu router unique-opened :default-active="$route.path">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/dashboard">数据看板</el-menu-item>

          <el-sub-menu index="content">
            <template #title>内容管理</template>
            <el-menu-item index="/content/moment">动态管理</el-menu-item>
            <el-menu-item index="/content/comment">评论管理</el-menu-item>
            <el-menu-item index="/content/tags">标签话题</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="moderation">
            <template #title>审核中心</template>
            <el-menu-item index="/moderation/queue">待审核队列</el-menu-item>
            <el-menu-item index="/moderation/reports">举报处理</el-menu-item>
            <el-menu-item index="/moderation/sensitive-words">敏感词库</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="users">
            <template #title>用户管理</template>
            <el-menu-item index="/users">用户列表</el-menu-item>
            <el-menu-item index="/users/friends">好友关系</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-body {
  min-height: 0;
}

.header {
  display: flex;
  align-items: center;
  padding-left: 30px;
  font-weight: 500;

  background-color: brown;
  color: whitesmoke;
  font-size: 18px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.aside {
  border-right: 1px solid var(--el-border-color-light);
}

.aside :deep(.el-menu) {
  border-right: none;
}

.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  padding: 0 12px; /* 左右对称留白，不破坏居中 */
  justify-content: flex-start;
  font-size: 15px;
  font-weight: 700; /* ← 加粗，500 不够明显 */
  letter-spacing: 1px;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  color: #333;
}

/* 选中项更明显一点（可选） */
.aside :deep(.el-menu-item.is-active) {
  color: brown;
  background: #fdf2f2;
}

.main {
  background: var(--el-fill-color-lighter);
  min-width: 0;
  overflow: auto;
}
</style>

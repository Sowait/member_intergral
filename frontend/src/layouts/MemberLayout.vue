<template>
  <div class="member-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">
          <h3>会员中心</h3>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/member/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          <el-menu-item index="/member/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="/member/points">
            <el-icon><Coin /></el-icon>
            <span>积分记录</span>
          </el-menu-item>
          <el-menu-item index="/member/exchange">
            <el-icon><Present /></el-icon>
            <span>积分兑换</span>
          </el-menu-item>
          <el-menu-item index="/member/orders">
            <el-icon><ShoppingCart /></el-icon>
            <span>消费记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header>
          <div class="header-left">
            <span class="welcome">欢迎，{{ user?.username }}</span>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" icon="User" />
                <span class="username">{{ user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { House, User, Coin, Present, ShoppingCart, ArrowDown } from '@element-plus/icons-vue'

export default {
  name: 'MemberLayout',
  components: {
    House, User, Coin, Present, ShoppingCart, ArrowDown
  },
  setup() {
    const router = useRouter()
    const store = useStore()

    const user = computed(() => store.state.user)

    const handleCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/member/profile')
          break
        case 'logout':
          handleLogout()
          break
      }
    }

    const handleLogout = () => {
      store.dispatch('logout')
      ElMessage.success('退出登录成功')
      router.push('/login')
    }

    onMounted(() => {
      // 检查用户角色
      if (user.value?.role !== 'MEMBER') {
        router.push('/login')
      }
    })

    return {
      user,
      handleCommand
    }
  }
}
</script>

<style scoped>
.member-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
  margin-bottom: 10px;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left .welcome {
  color: #606266;
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.username {
  font-size: 14px;
}

.el-main {
  background-color: #f5f5f5;
  padding: 20px;
}
</style>
<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon size="40"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalMembers }}</div>
              <div class="stat-label">总会员数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon points">
              <el-icon size="40"><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalPoints }}</div>
              <div class="stat-label">总积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon orders">
              <el-icon size="40"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon exchanges">
              <el-icon size="40"><Present /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalExchanges }}</div>
              <div class="stat-label">总兑换数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>会员等级分布</span>
            </div>
          </template>
          <div class="level-chart">
            <div v-for="level in levelDistribution" :key="level.level" class="level-item">
              <div class="level-info">
                <span class="level-name">{{ level.name }}</span>
                <span class="level-count">{{ level.count }}人</span>
              </div>
              <el-progress 
                :percentage="level.percentage" 
                :color="level.color"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <div class="recent-activities">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon">
                <el-icon><component :is="getActivityIcon(activity.type)" /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新会员</span>
              <el-button type="text" @click="$router.push('/admin/members')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="latestMembers" style="width: 100%">
            <el-table-column prop="memberCode" label="会员号" width="120" />
            <el-table-column prop="username" label="用户名" width="120">
              <template #default="scope">
                {{ scope.row.user?.username }}
              </template>
            </el-table-column>
            <el-table-column prop="level" label="等级" width="100">
              <template #default="scope">
                <el-tag :type="getLevelTagType(scope.row.level)">
                  {{ getLevelText(scope.row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalPoints" label="总积分" width="100" />
            <el-table-column prop="createTime" label="注册时间">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门商品</span>
              <el-button type="text" @click="$router.push('/admin/products')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="popularProducts" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="pointsRequired" label="所需积分" width="100" />
            <el-table-column prop="stockQuantity" label="库存" width="80" />
            <el-table-column prop="exchangeCount" label="兑换次数" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { User, Coin, ShoppingCart, Present, Plus, ShoppingBag, Trophy } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'AdminDashboard',
  components: {
    User, Coin, ShoppingCart, Present, Plus, ShoppingBag, Trophy
  },
  setup() {
    const statistics = ref({
      totalMembers: 0,
      totalPoints: 0,
      totalOrders: 0,
      totalExchanges: 0
    })

    const levelDistribution = ref([
      { level: 'BRONZE', name: '青铜会员', count: 0, percentage: 0, color: '#CD7F32' },
      { level: 'SILVER', name: '白银会员', count: 0, percentage: 0, color: '#C0C0C0' },
      { level: 'GOLD', name: '黄金会员', count: 0, percentage: 0, color: '#FFD700' },
      { level: 'PLATINUM', name: '铂金会员', count: 0, percentage: 0, color: '#E5E4E2' }
    ])

    const recentActivities = ref([])
    const latestMembers = ref([])
    const popularProducts = ref([])

    const loadStatistics = async () => {
      try {
        const response = await axios.get('/dashboard/statistics')
        if (response.data.code === 200) {
          statistics.value = response.data.data
        }
      } catch (error) {
        console.error('获取统计数据失败', error)
      }
    }

    const loadLevelDistribution = async () => {
      try {
        const response = await axios.get('/dashboard/level-distribution')
        if (response.data.code === 200) {
          const data = response.data.data
          const distribution = data.distribution
          
          // 更新各等级数据
          levelDistribution.value.forEach(level => {
            const levelData = distribution[level.level]
            if (levelData) {
              level.count = levelData.count
              level.percentage = levelData.percentage
            }
          })
        }
      } catch (error) {
        console.error('获取等级分布失败', error)
      }
    }

    const loadRecentActivities = async () => {
      try {
        // 模拟数据
        recentActivities.value = [
          { id: 1, type: 'register', title: '新会员注册', description: '用户 张三 注册成为会员', time: new Date(Date.now() - 1000 * 60 * 5) },
          { id: 2, type: 'order', title: '新订单', description: '订单 ORD202310250001 已创建', time: new Date(Date.now() - 1000 * 60 * 15) },
          { id: 3, type: 'exchange', title: '积分兑换', description: '李四 兑换了商品：星巴克咖啡券', time: new Date(Date.now() - 1000 * 60 * 30) },
          { id: 4, type: 'level', title: '会员升级', description: '王五 从青铜会员升级为白银会员', time: new Date(Date.now() - 1000 * 60 * 60) }
        ]
      } catch (error) {
        console.error('获取最近活动失败', error)
      }
    }

    const loadLatestMembers = async () => {
      try {
        const response = await axios.get('/member/list', {
          params: { current: 1, size: 5 }
        })
        if (response.data.code === 200) {
          latestMembers.value = response.data.data.records || []
        }
      } catch (error) {
        console.error('获取最新会员失败', error)
      }
    }

    const loadPopularProducts = async () => {
      try {
        // 模拟数据
        popularProducts.value = [
          { id: 1, productName: '星巴克咖啡券', pointsRequired: 500, stockQuantity: 50, exchangeCount: 120, status: 'ACTIVE' },
          { id: 2, productName: '电影票', pointsRequired: 800, stockQuantity: 30, exchangeCount: 85, status: 'ACTIVE' },
          { id: 3, productName: '购物券', pointsRequired: 300, stockQuantity: 0, exchangeCount: 200, status: 'INACTIVE' }
        ]
      } catch (error) {
        console.error('获取热门商品失败', error)
      }
    }

    const getActivityIcon = (type) => {
      const iconMap = {
        'register': 'User',
        'order': 'ShoppingCart',
        'exchange': 'Present',
        'level': 'Trophy'
      }
      return iconMap[type] || 'Plus'
    }

    const getLevelText = (level) => {
      const levelMap = {
        'BRONZE': '青铜',
        'SILVER': '白银',
        'GOLD': '黄金',
        'PLATINUM': '铂金'
      }
      return levelMap[level] || '普通'
    }

    const getLevelTagType = (level) => {
      const typeMap = {
        'BRONZE': 'info',
        'SILVER': '',
        'GOLD': 'warning',
        'PLATINUM': 'success'
      }
      return typeMap[level] || 'info'
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadStatistics()
      loadLevelDistribution()
      loadRecentActivities()
      loadLatestMembers()
      loadPopularProducts()
    })

    return {
      statistics,
      levelDistribution,
      recentActivities,
      latestMembers,
      popularProducts,
      getActivityIcon,
      getLevelText,
      getLevelTagType,
      formatTime
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #409EFF;
}

.stat-icon.points {
  background: #f0f9ff;
  color: #67C23A;
}

.stat-icon.orders {
  background: #fdf6ec;
  color: #E6A23C;
}

.stat-icon.exchanges {
  background: #fef0f0;
  color: #F56C6C;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.level-chart {
  padding: 20px 0;
}

.level-item {
  margin-bottom: 20px;
}

.level-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.level-name {
  color: #303133;
  font-weight: 500;
}

.level-count {
  color: #909399;
}

.recent-activities {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.activity-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.activity-time {
  color: #909399;
  font-size: 12px;
}
</style>
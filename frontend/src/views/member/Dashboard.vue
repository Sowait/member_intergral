<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 会员信息卡片 -->
      <el-col :span="8">
        <el-card class="member-card">
          <template #header>
            <div class="card-header">
              <span>会员信息</span>
            </div>
          </template>
          <div class="member-info">
            <div class="avatar">
              <el-avatar :size="80" icon="User" />
            </div>
            <div class="info">
              <h3>{{ memberInfo.user?.username }}</h3>
              <p>会员号：{{ memberInfo.member?.memberCode }}</p>
              <p>等级：{{ getLevelText(memberInfo.member?.level) }}</p>
              <p>手机：{{ memberInfo.user?.phone }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 积分信息卡片 -->
      <el-col :span="8">
        <el-card class="points-card">
          <template #header>
            <div class="card-header">
              <span>积分信息</span>
            </div>
          </template>
          <div class="points-info">
            <div class="points-item">
              <div class="points-value">{{ memberInfo.member?.availablePoints || 0 }}</div>
              <div class="points-label">可用积分</div>
            </div>
            <div class="points-item">
              <div class="points-value">{{ memberInfo.member?.totalPoints || 0 }}</div>
              <div class="points-label">总积分</div>
            </div>
          </div>
          <div class="level-progress">
            <el-progress 
              :percentage="getLevelProgress(memberInfo.member?.totalPoints || 0)" 
              :color="getLevelColor(memberInfo.member?.level)"
            />
            <p class="progress-text">距离下一级还需 {{ getNextLevelPoints(memberInfo.member?.totalPoints || 0) }} 积分</p>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="8">
        <el-card class="actions-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="actions">
            <el-button type="primary" @click="$router.push('/member/exchange')" style="width: 100%; margin-bottom: 10px;">
              积分兑换
            </el-button>
            <el-button type="success" @click="$router.push('/member/points')" style="width: 100%; margin-bottom: 10px;">
              积分记录
            </el-button>
            <el-button type="info" @click="$router.push('/member/orders')" style="width: 100%;">
              消费记录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近积分记录</span>
              <el-button type="text" @click="$router.push('/member/points')">查看全部</el-button>
            </div>
          </template>
          <div class="recent-records">
            <div v-for="record in recentPointRecords.slice(0, 5)" :key="record.id" class="record-item">
              <div class="record-info">
                <div class="record-desc">{{ record.description }}</div>
                <div class="record-time">{{ formatTime(record.createTime) }}</div>
              </div>
              <div class="record-points" :class="{ 'positive': record.pointsChange > 0, 'negative': record.pointsChange < 0 }">
                {{ record.pointsChange > 0 ? '+' : '' }}{{ record.pointsChange }}
              </div>
            </div>
            <div v-if="recentPointRecords.length === 0" class="no-data">
              暂无积分记录
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近消费记录</span>
              <el-button type="text" @click="$router.push('/member/orders')">查看全部</el-button>
            </div>
          </template>
          <div class="recent-records">
            <div v-for="order in recentOrders.slice(0, 5)" :key="order.id" class="record-item">
              <div class="record-info">
                <div class="record-desc">{{ order.description || '消费' }}</div>
                <div class="record-time">{{ formatTime(order.orderTime) }}</div>
              </div>
              <div class="record-amount">
                ¥{{ order.amount }} ({{ order.pointsEarned }}积分)
              </div>
            </div>
            <div v-if="recentOrders.length === 0" class="no-data">
              暂无消费记录
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'MemberDashboard',
  setup() {
    const memberInfo = ref({})
    const recentPointRecords = ref([])
    const recentOrders = ref([])

    const loadMemberInfo = async () => {
      try {
        const response = await axios.get('/member/profile')
        if (response.data.code === 200) {
          memberInfo.value = response.data.data
        }
      } catch (error) {
        ElMessage.error('获取会员信息失败')
      }
    }

    const loadRecentPointRecords = async () => {
      try {
        const response = await axios.get('/member/points')
        if (response.data.code === 200) {
          recentPointRecords.value = response.data.data.records || []
        }
      } catch (error) {
        console.error('获取积分记录失败', error)
        recentPointRecords.value = []
      }
    }

    const loadRecentOrders = async () => {
      try {
        const response = await axios.get('/orders/my-orders')
        if (response.data.code === 200) {
          recentOrders.value = response.data.data.records || []
        }
      } catch (error) {
        console.error('获取消费记录失败', error)
        recentOrders.value = []
      }
    }

    const getLevelText = (level) => {
      const levelMap = {
        'BRONZE': '青铜会员',
        'SILVER': '白银会员',
        'GOLD': '黄金会员',
        'PLATINUM': '铂金会员'
      }
      return levelMap[level] || '普通会员'
    }

    const getLevelProgress = (totalPoints) => {
      if (totalPoints >= 5000) return 100
      if (totalPoints >= 2000) return ((totalPoints - 2000) / 3000) * 100
      if (totalPoints >= 500) return ((totalPoints - 500) / 1500) * 100
      return (totalPoints / 500) * 100
    }

    const getLevelColor = (level) => {
      const colorMap = {
        'BRONZE': '#CD7F32',
        'SILVER': '#C0C0C0',
        'GOLD': '#FFD700',
        'PLATINUM': '#E5E4E2'
      }
      return colorMap[level] || '#409EFF'
    }

    const getNextLevelPoints = (totalPoints) => {
      if (totalPoints >= 5000) return 0
      if (totalPoints >= 2000) return 5000 - totalPoints
      if (totalPoints >= 500) return 2000 - totalPoints
      return 500 - totalPoints
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadMemberInfo()
      loadRecentPointRecords()
      loadRecentOrders()
    })

    return {
      memberInfo,
      recentPointRecords,
      recentOrders,
      getLevelText,
      getLevelProgress,
      getLevelColor,
      getNextLevelPoints,
      formatTime
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.info p {
  margin: 5px 0;
  color: #666;
}

.points-info {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.points-item {
  text-align: center;
}

.points-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.points-label {
  color: #666;
  margin-top: 5px;
}

.level-progress {
  margin-top: 20px;
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.actions {
  display: flex;
  flex-direction: column;
}

.recent-records {
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-info {
  flex: 1;
}

.record-desc {
  color: #333;
  margin-bottom: 5px;
}

.record-time {
  color: #999;
  font-size: 12px;
}

.record-points.positive {
  color: #67C23A;
  font-weight: bold;
}

.record-points.negative {
  color: #F56C6C;
  font-weight: bold;
}

.record-amount {
  color: #409EFF;
  font-weight: bold;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
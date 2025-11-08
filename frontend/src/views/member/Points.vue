<template>
  <div class="points">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分记录</span>
          <div class="points-summary">
            <span>可用积分：</span>
            <span class="points-value">{{ memberInfo.member?.availablePoints || 0 }}</span>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="类型">
            <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px">
              <el-option label="获得积分" value="EARN" />
              <el-option label="消费积分" value="CONSUME" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadPointRecords">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 积分记录列表 -->
      <el-table :data="pointRecords" style="width: 100%">
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'EARN' ? 'success' : 'danger'">
              {{ scope.row.type === 'EARN' ? '获得' : '消费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsChange" label="积分变化" width="120">
          <template #default="scope">
            <span :class="{ 'positive': scope.row.pointsChange > 0, 'negative': scope.row.pointsChange < 0 }">
              {{ scope.row.pointsChange > 0 ? '+' : '' }}{{ scope.row.pointsChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <div v-if="pointRecords.length === 0" class="no-data">
        暂无积分记录
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value positive">{{ statistics.totalEarned || 0 }}</div>
            <div class="stat-label">总获得积分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value negative">{{ statistics.totalConsumed || 0 }}</div>
            <div class="stat-label">总消费积分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ memberInfo.member?.totalPoints || 0 }}</div>
            <div class="stat-label">累计总积分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'MemberPoints',
  setup() {
    const pointRecords = ref([])
    const memberInfo = reactive({ member: {} })
    const statistics = reactive({
      totalEarned: 0,
      totalConsumed: 0
    })

    const filterForm = reactive({
      type: '',
      dateRange: []
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const loadMemberInfo = async () => {
      try {
        const response = await axios.get('/member/profile')
        if (response.data.code === 200) {
          memberInfo.member = response.data.data.member
        }
      } catch (error) {
        console.error('获取会员信息失败', error)
      }
    }

    const loadPointRecords = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          type: filterForm.type || undefined
        }

        if (filterForm.dateRange && filterForm.dateRange.length === 2) {
          params.startDate = filterForm.dateRange[0]
          params.endDate = filterForm.dateRange[1]
        }

        const response = await axios.get('/member/points', { params })
        if (response.data.code === 200) {
          pointRecords.value = response.data.data.records || response.data.data
          pagination.total = response.data.data.total || response.data.data.length
          
          // 计算统计信息
          calculateStatistics()
        }
      } catch (error) {
        ElMessage.error('获取积分记录失败')
      }
    }

    const calculateStatistics = () => {
      let totalEarned = 0
      let totalConsumed = 0

      pointRecords.value.forEach(record => {
        if (record.pointsChange > 0) {
          totalEarned += record.pointsChange
        } else {
          totalConsumed += Math.abs(record.pointsChange)
        }
      })

      statistics.totalEarned = totalEarned
      statistics.totalConsumed = totalConsumed
    }

    const resetFilter = () => {
      filterForm.type = ''
      filterForm.dateRange = []
      pagination.current = 1
      loadPointRecords()
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      loadPointRecords()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadPointRecords()
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadMemberInfo()
      loadPointRecords()
    })

    return {
      pointRecords,
      memberInfo,
      statistics,
      filterForm,
      pagination,
      loadPointRecords,
      resetFilter,
      handleSizeChange,
      handleCurrentChange,
      formatTime
    }
  }
}
</script>

<style scoped>
.points {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-summary {
  color: #666;
}

.points-value {
  color: #409EFF;
  font-weight: bold;
  font-size: 18px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.positive {
  color: #67C23A;
  font-weight: bold;
}

.negative {
  color: #F56C6C;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 40px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}
</style>
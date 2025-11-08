<template>
  <div class="orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消费记录</span>
          <div class="total-amount">
            累计消费：<span class="amount-value">¥{{ statistics.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="门店">
            <el-select v-model="filterForm.storeId" placeholder="全部门店" clearable style="width: 150px">
              <el-option
                v-for="store in stores"
                :key="store.id"
                :label="store.storeName"
                :value="store.id"
              />
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
          <el-form-item label="金额范围">
            <el-input-number
              v-model="filterForm.minAmount"
              placeholder="最小金额"
              :min="0"
              :precision="2"
              style="width: 120px"
            />
            <span style="margin: 0 10px;">-</span>
            <el-input-number
              v-model="filterForm.maxAmount"
              placeholder="最大金额"
              :min="0"
              :precision="2"
              style="width: 120px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadOrders">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 消费记录列表 -->
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="220" />
        <el-table-column prop="storeName" label="门店" width="150">
          <template #default="scope">
            {{ getStoreName(scope.row.storeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="消费金额" width="120">
          <template #default="scope">
            <span class="amount">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="获得积分" width="100">
          <template #default="scope">
            <span class="points">+{{ scope.row.pointsEarned }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="orderTime" label="消费时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.orderTime) }}
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

      <div v-if="orders.length === 0" class="no-data">
        暂无消费记录
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.orderCount }}</div>
            <div class="stat-label">消费次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">¥{{ statistics.totalAmount.toFixed(2) }}</div>
            <div class="stat-label">累计消费</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalPoints }}</div>
            <div class="stat-label">累计积分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">¥{{ statistics.averageAmount.toFixed(2) }}</div>
            <div class="stat-label">平均消费</div>
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
  name: 'MemberOrders',
  setup() {
    const orders = ref([])
    const stores = ref([])
    const storeMap = ref(new Map())

    const filterForm = reactive({
      storeId: '',
      dateRange: [],
      minAmount: null,
      maxAmount: null
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const statistics = reactive({
      orderCount: 0,
      totalAmount: 0,
      totalPoints: 0,
      averageAmount: 0
    })

    const loadOrders = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          storeId: filterForm.storeId || undefined
        }

        if (filterForm.dateRange && filterForm.dateRange.length === 2) {
          params.startDate = filterForm.dateRange[0]
          params.endDate = filterForm.dateRange[1]
        }

        if (filterForm.minAmount !== null) {
          params.minAmount = filterForm.minAmount
        }

        if (filterForm.maxAmount !== null) {
          params.maxAmount = filterForm.maxAmount
        }

        const response = await axios.get('/orders/my-orders', { params })
        if (response.data.code === 200) {
          orders.value = response.data.data.records || response.data.data
          pagination.total = response.data.data.total || response.data.data.length
          
          // 同时加载统计数据
          await loadStatistics()
        }
      } catch (error) {
        ElMessage.error('获取消费记录失败')
      }
    }

    const loadStores = async () => {
      try {
        const response = await axios.get('/orders/stores')
        if (response.data.code === 200) {
          stores.value = response.data.data
          
          // 构建门店映射
          stores.value.forEach(store => {
            storeMap.value.set(store.id, store.storeName)
          })
        }
      } catch (error) {
        console.error('获取门店列表失败', error)
      }
    }

    const loadStatistics = async () => {
      try {
        const params = {}
        
        if (filterForm.storeId) {
          params.storeId = filterForm.storeId
        }
        
        if (filterForm.dateRange && filterForm.dateRange.length === 2) {
          params.startDate = filterForm.dateRange[0]
          params.endDate = filterForm.dateRange[1]
        }
        
        if (filterForm.minAmount !== null) {
          params.minAmount = filterForm.minAmount
        }
        
        if (filterForm.maxAmount !== null) {
          params.maxAmount = filterForm.maxAmount
        }

        const response = await axios.get('/orders/my-statistics', { params })
        if (response.data.code === 200) {
          const data = response.data.data
          statistics.orderCount = data.orderCount || 0
          statistics.totalAmount = data.totalAmount || 0
          statistics.totalPoints = data.totalPoints || 0
          statistics.averageAmount = data.averageAmount || 0
        }
      } catch (error) {
        console.error('获取消费统计失败', error)
      }
    }

    const resetFilter = () => {
      filterForm.storeId = ''
      filterForm.dateRange = []
      filterForm.minAmount = null
      filterForm.maxAmount = null
      pagination.current = 1
      loadOrders()
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      loadOrders()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadOrders()
    }

    const getStoreName = (storeId) => {
      return storeMap.value.get(storeId) || '未知门店'
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(async () => {
      await loadStores()
      await loadOrders()
      // 确保统计数据也被加载
      await loadStatistics()
    })

    return {
      orders,
      stores,
      filterForm,
      pagination,
      statistics,
      loadOrders,
      resetFilter,
      handleSizeChange,
      handleCurrentChange,
      getStoreName,
      formatTime,
      orderCount: statistics.orderCount,
      totalAmount: statistics.totalAmount,
      totalPoints: statistics.totalPoints,
      averageAmount: statistics.averageAmount
    }
  }
}
</script>

<style scoped>
.orders {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-amount {
  color: #666;
}

.amount-value {
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

.amount {
  color: #E6A23C;
  font-weight: bold;
}

.points {
  color: #67C23A;
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
  color: #409EFF;
}

.stat-label {
  color: #666;
  font-size: 14px;
}
</style>
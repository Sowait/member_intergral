<template>
  <div class="orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消费记录管理</span>
          <el-button type="primary" @click="handleCreateOrder">新增消费记录</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="订单号">
            <el-input v-model="filterForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
      
          <el-form-item label="门店">
            <el-select 
              v-model="filterForm.storeId" 
              placeholder="全部门店" 
              clearable
              style="width: 200px"
              @change="handleStoreChange"
            >
              <el-option
                v-for="store in stores"
                :key="store.id"
                :label="store.storeName"
                :value="store.id"
              />
            </el-select>
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
          <el-form-item label="消费时间">
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
            <el-button type="primary" @click="loadOrders">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 消费记录列表 -->
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="memberCode" label="会员号" width="120">
          <template #default="scope">
            {{ scope.row.member?.memberCode }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="scope">
            {{ scope.row.member?.user?.username }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              详情
            </el-button>
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
    </el-card>

    <!-- 新增消费记录对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增消费记录" width="50%">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="会员号" prop="memberCode">
          <el-input v-model="createForm.memberCode" placeholder="请输入会员号" />
        </el-form-item>
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="createForm.storeId" placeholder="请选择门店" style="width: 100%">
            <el-option
              v-for="store in stores"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消费金额" prop="amount">
          <el-input-number v-model="createForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" placeholder="请输入消费描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="60%">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="消费时间">{{ formatTime(currentOrder.orderTime) }}</el-descriptions-item>
          <el-descriptions-item label="会员号">{{ currentOrder.member?.memberCode }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.member?.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="门店">{{ getStoreName(currentOrder.storeId) }}</el-descriptions-item>
          <el-descriptions-item label="消费金额">¥{{ currentOrder.amount }}</el-descriptions-item>
          <el-descriptions-item label="获得积分">{{ currentOrder.pointsEarned }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentOrder.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'AdminOrders',
  setup() {
    const orders = ref([])
    const stores = ref([])
    const storeMap = ref(new Map())
    const currentOrder = ref(null)
    const createFormRef = ref()
    const createDialogVisible = ref(false)
    const detailDialogVisible = ref(false)

    const filterForm = reactive({
      orderNo: '',
      memberCode: '',
      storeId: '',
      minAmount: null,
      maxAmount: null,
      dateRange: []
    })

    const createForm = reactive({
      memberCode: '',
      storeId: '',
      amount: null,
      description: ''
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const createRules = {
      memberCode: [
        { required: true, message: '请输入会员号', trigger: 'blur' }
      ],
      storeId: [
        { required: true, message: '请选择门店', trigger: 'change' }
      ],
      amount: [
        { required: true, message: '请输入消费金额', trigger: 'blur' }
      ]
    }

    const loadOrders = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          storeId: filterForm.storeId || undefined,
          keyword: filterForm.orderNo || filterForm.memberCode || undefined
        }

        if (filterForm.minAmount !== null) {
          params.minAmount = filterForm.minAmount
        }

        if (filterForm.maxAmount !== null) {
          params.maxAmount = filterForm.maxAmount
        }

        if (filterForm.dateRange && filterForm.dateRange.length === 2) {
          params.startDate = filterForm.dateRange[0]
          params.endDate = filterForm.dateRange[1]
        }

        const response = await axios.get('/orders/list', { params })
        if (response.data.code === 200) {
          orders.value = response.data.data.records || []
          pagination.total = response.data.data.total || 0
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
        ElMessage.error('获取门店列表失败')
      }
    }

    const resetFilter = () => {
      filterForm.orderNo = ''
      filterForm.memberCode = ''
      filterForm.storeId = ''
      filterForm.minAmount = null
      filterForm.maxAmount = null
      filterForm.dateRange = []
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

    const handleCreateOrder = () => {
      createForm.memberCode = ''
      createForm.storeId = ''
      createForm.amount = null
      createForm.description = ''
      createDialogVisible.value = true
    }

    const handleSaveCreate = async () => {
      try {
        await createFormRef.value.validate()
        
        // 根据会员号查找会员ID
        const memberResponse = await axios.get('/member/list', {
          params: { keyword: createForm.memberCode, size: 1 }
        })
        
        if (memberResponse.data.code !== 200 || !memberResponse.data.data.records.length) {
          ElMessage.error('会员号不存在')
          return
        }
        
        const memberId = memberResponse.data.data.records[0].id
        
        const response = await axios.post('/orders', {
          memberId,
          storeId: createForm.storeId,
          amount: createForm.amount,
          description: createForm.description
        })
        
        if (response.data.code === 200) {
          ElMessage.success('创建成功')
          createDialogVisible.value = false
          loadOrders()
        } else {
          ElMessage.error(response.data.message || '创建失败')
        }
      } catch (error) {
        ElMessage.error('创建失败')
      }
    }

    const handleView = (order) => {
      currentOrder.value = order
      detailDialogVisible.value = true
    }

    const handleStoreChange = (value) => {
      console.log('门店选择变化:', value)
      // 可以在这里添加额外的处理逻辑，比如自动触发查询
      // loadOrders()
    }

    const getStoreName = (storeId) => {
      return storeMap.value.get(storeId) || '未知门店'
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadStores()
      loadOrders()
    })

    return {
      orders,
      stores,
      currentOrder,
      createFormRef,
      createDialogVisible,
      detailDialogVisible,
      filterForm,
      createForm,
      pagination,
      createRules,
      loadOrders,
      resetFilter,
      handleSizeChange,
      handleCurrentChange,
      handleCreateOrder,
      handleSaveCreate,
      handleView,
      handleStoreChange,
      getStoreName,
      formatTime
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

.order-detail {
  padding: 20px 0;
}
</style>
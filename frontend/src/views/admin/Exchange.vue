<template>
  <div class="exchange">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分兑换管理</span>
          <el-button type="primary" @click="handleCreateProduct">新增商品</el-button>
        </div>
      </template>

      <!-- 商品管理标签页 -->
      <el-tabs v-model="activeTab">
        <!-- 商品列表 -->
        <el-tab-pane label="商品管理" name="products">
          <!-- 筛选条件 -->
          <div class="filter-section">
            <el-form :inline="true" :model="filterForm" class="filter-form">
              <el-form-item label="商品名称">
                <el-input v-model="filterForm.keyword" placeholder="请输入商品名称" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="filterForm.status" placeholder="全部状态" clearable  style="width: 100px">
                  <el-option label="上架" value="ACTIVE" />
                  <el-option label="下架" value="INACTIVE" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadProducts">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 商品列表 -->
          <el-table :data="products" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" width="200" />
            <el-table-column prop="description" label="描述" min-width="250" />
            <el-table-column prop="pointsRequired" label="所需积分" width="120" />
            <el-table-column prop="stockQuantity" label="库存" width="100">
              <template #default="scope">
                <span :class="{ 'low-stock': scope.row.stockQuantity <= 10 }">
                  {{ scope.row.stockQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="exchangeCount" label="兑换次数" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditProduct(scope.row)">
                  编辑
                </el-button>
                <el-button 
                  :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" 
                  size="small" 
                  @click="handleToggleStatus(scope.row)"
                >
                  {{ scope.row.status === 'ACTIVE' ? '下架' : '上架' }}
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteProduct(scope.row)">
                  删除
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
        </el-tab-pane>

        <!-- 兑换记录 -->
        <el-tab-pane label="兑换记录" name="records">
          <!-- 兑换记录筛选 -->
          <div class="filter-section">
            <el-form :inline="true" :model="recordFilterForm" class="filter-form">
              <el-form-item label="商品">
                <el-select v-model="recordFilterForm.productId" placeholder="全部商品" clearable   style="width: 150px">
                  <el-option
                    v-for="product in allProducts"
                    :key="product.id"
                    :label="product.productName"
                    :value="product.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="领取信息">
                <el-input v-model="recordFilterForm.keyword" placeholder="请输入领取信息" clearable />
              </el-form-item>
              <el-form-item label="兑换时间">
                <el-date-picker
                  v-model="recordFilterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadExchangeRecords">查询</el-button>
                <el-button @click="resetRecordFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 兑换记录列表 -->
          <el-table :data="exchangeRecords" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" width="200">
              <template #default="scope">
                {{ getProductName(scope.row.productId) }}
              </template>
            </el-table-column>
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
            <el-table-column prop="pointsUsed" label="使用积分" width="100">
              <template #default="scope">
                <span class="points-used">-{{ scope.row.pointsUsed }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="receiveInfo" label="领取信息" min-width="200" />
            <el-table-column prop="exchangeTime" label="兑换时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.exchangeTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewRecord(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="recordPagination.current"
              v-model:page-size="recordPagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="recordPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRecordSizeChange"
              @current-change="handleRecordCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑商品对话框 -->
    <el-dialog 
      v-model="productDialogVisible" 
      :title="isEdit ? '编辑商品' : '新增商品'" 
      width="60%"
    >
      <el-form :model="productForm" :rules="productRules" ref="productFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="所需积分" prop="pointsRequired">
          <el-input-number v-model="productForm.pointsRequired" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number v-model="productForm.stockQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="productForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="productForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- 兑换记录详情对话框 -->
    <el-dialog v-model="recordDialogVisible" title="兑换记录详情" width="50%">
      <div v-if="currentRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称">{{ getProductName(currentRecord.productId) }}</el-descriptions-item>
          <el-descriptions-item label="会员号">{{ currentRecord.member?.memberCode }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentRecord.member?.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="使用积分">{{ currentRecord.pointsUsed }}</el-descriptions-item>
          <el-descriptions-item label="领取信息" :span="2">{{ currentRecord.receiveInfo }}</el-descriptions-item>
          <el-descriptions-item label="兑换时间" :span="2">{{ formatTime(currentRecord.exchangeTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'AdminExchange',
  setup() {
    const activeTab = ref('products')
    const products = ref([])
    const allProducts = ref([])
    const exchangeRecords = ref([])
    const currentRecord = ref(null)
    const productFormRef = ref()
    const productDialogVisible = ref(false)
    const recordDialogVisible = ref(false)
    const isEdit = ref(false)
    const productMap = ref(new Map())

    const filterForm = reactive({
      keyword: '',
      status: ''
    })

    const recordFilterForm = reactive({
      productId: '',
      keyword: '',
      dateRange: []
    })

    const productForm = reactive({
      productName: '',
      description: '',
      pointsRequired: null,
      stockQuantity: null,
      imageUrl: '',
      status: 'ACTIVE'
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const recordPagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const productRules = {
      productName: [
        { required: true, message: '请输入商品名称', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入商品描述', trigger: 'blur' }
      ],
      pointsRequired: [
        { required: true, message: '请输入所需积分', trigger: 'blur' }
      ],
      stockQuantity: [
        { required: true, message: '请输入库存数量', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    const loadProducts = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          keyword: filterForm.keyword || undefined,
          status: filterForm.status || undefined
        }

        const response = await axios.get('/exchange/products/manage', { params })
        if (response.data.code === 200) {
          products.value = response.data.data.records || []
          pagination.total = response.data.data.total || 0
        }
      } catch (error) {
        ElMessage.error('获取商品列表失败')
      }
    }

    const loadAllProducts = async () => {
      try {
        const response = await axios.get('/exchange/products')
        if (response.data.code === 200) {
          allProducts.value = response.data.data
          
          // 构建商品映射
          allProducts.value.forEach(product => {
            productMap.value.set(product.id, product.productName)
          })
        }
      } catch (error) {
        console.error('获取所有商品失败', error)
      }
    }

    const loadExchangeRecords = async () => {
      try {
        const params = {
          current: recordPagination.current,
          size: recordPagination.size,
          productId: recordFilterForm.productId || undefined,
          keyword: recordFilterForm.keyword || undefined
        }

        if (recordFilterForm.dateRange && recordFilterForm.dateRange.length === 2) {
          params.startDate = recordFilterForm.dateRange[0]
          params.endDate = recordFilterForm.dateRange[1]
        }

        const response = await axios.get('/exchange/records/list', { params })
        if (response.data.code === 200) {
          exchangeRecords.value = response.data.data.records || []
          recordPagination.total = response.data.data.total || 0
        }
      } catch (error) {
        ElMessage.error('获取兑换记录失败')
      }
    }

    const resetFilter = () => {
      filterForm.keyword = ''
      filterForm.status = ''
      pagination.current = 1
      loadProducts()
    }

    const resetRecordFilter = () => {
      recordFilterForm.productId = ''
      recordFilterForm.keyword = ''
      recordFilterForm.dateRange = []
      recordPagination.current = 1
      loadExchangeRecords()
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      loadProducts()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadProducts()
    }

    const handleRecordSizeChange = (size) => {
      recordPagination.size = size
      recordPagination.current = 1
      loadExchangeRecords()
    }

    const handleRecordCurrentChange = (current) => {
      recordPagination.current = current
      loadExchangeRecords()
    }

    const handleCreateProduct = () => {
      isEdit.value = false
      productForm.productName = ''
      productForm.description = ''
      productForm.pointsRequired = null
      productForm.stockQuantity = null
      productForm.imageUrl = ''
      productForm.status = 'ACTIVE'
      productDialogVisible.value = true
    }

    const handleEditProduct = (product) => {
      isEdit.value = true
      Object.assign(productForm, product)
      productDialogVisible.value = true
    }

    const handleSaveProduct = async () => {
      try {
        await productFormRef.value.validate()
        
        let response
        if (isEdit.value) {
          response = await axios.put(`/exchange/products/${productForm.id}`, productForm)
        } else {
          response = await axios.post('/exchange/products', productForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          productDialogVisible.value = false
          loadProducts()
          loadAllProducts()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    const handleToggleStatus = async (product) => {
      try {
        const newStatus = product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
        const response = await axios.put(`/exchange/products/${product.id}`, {
          ...product,
          status: newStatus
        })
        
        if (response.data.code === 200) {
          ElMessage.success(newStatus === 'ACTIVE' ? '上架成功' : '下架成功')
          loadProducts()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    const handleDeleteProduct = async (product) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除商品 ${product.productName} 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await axios.delete(`/exchange/products/${product.id}`)
        if (response.data.code === 200) {
          ElMessage.success('删除成功')
          loadProducts()
          loadAllProducts()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    const handleViewRecord = (record) => {
      currentRecord.value = record
      recordDialogVisible.value = true
    }

    const getProductName = (productId) => {
      return productMap.value.get(productId) || '未知商品'
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadProducts()
      loadAllProducts()
      loadExchangeRecords()
    })

    return {
      activeTab,
      products,
      allProducts,
      exchangeRecords,
      currentRecord,
      productFormRef,
      productDialogVisible,
      recordDialogVisible,
      isEdit,
      filterForm,
      recordFilterForm,
      productForm,
      pagination,
      recordPagination,
      productRules,
      loadProducts,
      loadExchangeRecords,
      resetFilter,
      resetRecordFilter,
      handleSizeChange,
      handleCurrentChange,
      handleRecordSizeChange,
      handleRecordCurrentChange,
      handleCreateProduct,
      handleEditProduct,
      handleSaveProduct,
      handleToggleStatus,
      handleDeleteProduct,
      handleViewRecord,
      getProductName,
      formatTime
    }
  }
}
</script>

<style scoped>
.exchange {
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

.low-stock {
  color: #F56C6C;
  font-weight: bold;
}

.points-used {
  color: #F56C6C;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.record-detail {
  padding: 20px 0;
}
</style>
<template>
  <div class="mall">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分商城</span>
          <div class="points-display">
            可用积分：<span class="points-value">{{ memberInfo.member?.availablePoints || 0 }}</span>
          </div>
        </div>
      </template>

      <!-- 商品分类 -->
      <div class="category-section">
        <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
          <el-tab-pane label="全部商品" name="all" />
          <el-tab-pane label="餐饮券" name="food" />
          <el-tab-pane label="购物券" name="shopping" />
          <el-tab-pane label="娱乐券" name="entertainment" />
          <el-tab-pane label="生活服务" name="service" />
        </el-tabs>
      </div>

      <!-- 筛选和排序 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              prefix-icon="Search"
              @input="handleSearch"
            />
          </el-col>
          <el-col :span="8">
            <el-select v-model="sortBy" @change="handleSort" style="width: 100%">
              <el-option label="默认排序" value="default" />
              <el-option label="积分从低到高" value="points_asc" />
              <el-option label="积分从高到低" value="points_desc" />
              <el-option label="兑换最多" value="popular" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-select v-model="stockFilter" @change="handleStockFilter" style="width: 100%">
              <el-option label="全部库存" value="all" />
              <el-option label="仅显示有货" value="in_stock" />
              <el-option label="即将售罄" value="low_stock" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 商品列表 -->
      <div class="products-grid">
        <div v-for="product in filteredProducts" :key="product.id" class="product-card">
          <div class="product-image">
            <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.productName" />
            <div v-else class="no-image">
              <el-icon size="40"><Picture /></el-icon>
            </div>
            <div v-if="product.stockQuantity <= 10" class="low-stock-badge">
              仅剩{{ product.stockQuantity }}件
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.productName }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-meta">
              <div class="product-points">
                <span class="points-required">{{ product.pointsRequired }}</span>
                <span class="points-unit">积分</span>
              </div>
              <div class="product-stats">
                <span class="exchange-count">{{ product.exchangeCount }}人已兑换</span>
              </div>
            </div>
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="handleQuickExchange(product)"
                :disabled="product.stockQuantity <= 0 || (memberInfo.member?.availablePoints || 0) < product.pointsRequired"
              >
                {{ product.stockQuantity <= 0 ? '已售罄' : (memberInfo.member?.availablePoints || 0) < product.pointsRequired ? '积分不足' : '立即兑换' }}
              </el-button>
              <el-button size="small" @click="handleViewDetail(product)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredProducts.length === 0" class="no-data">
        <el-empty description="暂无相关商品" />
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[12, 24, 48]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商品详情" width="60%">
      <div v-if="currentProduct" class="product-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-image">
              <img v-if="currentProduct.imageUrl" :src="currentProduct.imageUrl" :alt="currentProduct.productName" />
              <div v-else class="no-image-large">
                <el-icon size="80"><Picture /></el-icon>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info">
              <h2>{{ currentProduct.productName }}</h2>
              <div class="detail-points">
                <span class="points-large">{{ currentProduct.pointsRequired }}</span>
                <span class="points-unit">积分</span>
              </div>
              <div class="detail-stock">
                库存：{{ currentProduct.stockQuantity }}件
                <span v-if="currentProduct.stockQuantity <= 10" class="low-stock-text">
                  （即将售罄）
                </span>
              </div>
              <div class="detail-stats">
                {{ currentProduct.exchangeCount }}人已兑换
              </div>
              <div class="detail-description">
                <h4>商品描述</h4>
                <p>{{ currentProduct.description }}</p>
              </div>
              <div class="detail-actions">
                <el-button 
                  type="primary" 
                  size="large"
                  @click="handleExchange(currentProduct)"
                  :disabled="currentProduct.stockQuantity <= 0 || (memberInfo.member?.availablePoints || 0) < currentProduct.pointsRequired"
                >
                  {{ currentProduct.stockQuantity <= 0 ? '已售罄' : (memberInfo.member?.availablePoints || 0) < currentProduct.pointsRequired ? '积分不足' : '立即兑换' }}
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'MemberMall',
  components: {
    Picture
  },
  setup() {
    const products = ref([])
    const memberInfo = reactive({ member: {} })
    const currentProduct = ref(null)
    const detailDialogVisible = ref(false)
    const activeCategory = ref('all')
    const searchKeyword = ref('')
    const sortBy = ref('default')
    const stockFilter = ref('all')

    const pagination = reactive({
      current: 1,
      size: 12,
      total: 0
    })

    const filteredProducts = computed(() => {
      let result = [...products.value]

      // 分类筛选
      if (activeCategory.value !== 'all') {
        result = result.filter(product => product.category === activeCategory.value)
      }

      // 搜索筛选
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(product => 
          product.productName.toLowerCase().includes(keyword) ||
          product.description.toLowerCase().includes(keyword)
        )
      }

      // 库存筛选
      if (stockFilter.value === 'in_stock') {
        result = result.filter(product => product.stockQuantity > 0)
      } else if (stockFilter.value === 'low_stock') {
        result = result.filter(product => product.stockQuantity > 0 && product.stockQuantity <= 10)
      }

      // 排序
      if (sortBy.value === 'points_asc') {
        result.sort((a, b) => a.pointsRequired - b.pointsRequired)
      } else if (sortBy.value === 'points_desc') {
        result.sort((a, b) => b.pointsRequired - a.pointsRequired)
      } else if (sortBy.value === 'popular') {
        result.sort((a, b) => b.exchangeCount - a.exchangeCount)
      }

      return result
    })

    const loadProducts = async () => {
      try {
        const response = await axios.get('/exchange/products')
        if (response.data.code === 200) {
          products.value = response.data.data.map(product => ({
            ...product,
            category: product.category || 'food' // 模拟分类数据
          }))
          pagination.total = products.value.length
        }
      } catch (error) {
        ElMessage.error('获取商品列表失败')
      }
    }

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

    const handleCategoryChange = () => {
      pagination.current = 1
    }

    const handleSearch = () => {
      pagination.current = 1
    }

    const handleSort = () => {
      pagination.current = 1
    }

    const handleStockFilter = () => {
      pagination.current = 1
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
    }

    const handleViewDetail = (product) => {
      currentProduct.value = product
      detailDialogVisible.value = true
    }

    const handleQuickExchange = async (product) => {
      await handleExchange(product)
    }

    const handleExchange = async (product) => {
      try {
        await ElMessageBox.confirm(
          `确定要用 ${product.pointsRequired} 积分兑换 ${product.productName} 吗？`,
          '确认兑换',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await axios.post('/exchange/exchange', {
          productId: product.id
        })

        if (response.data.code === 200) {
          ElMessage.success('兑换成功')
          detailDialogVisible.value = false
          
          // 刷新数据
          await loadProducts()
          await loadMemberInfo()
        } else {
          ElMessage.error(response.data.message || '兑换失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.response?.data?.message || '兑换失败')
        }
      }
    }

    onMounted(() => {
      loadProducts()
      loadMemberInfo()
    })

    return {
      products,
      memberInfo,
      currentProduct,
      detailDialogVisible,
      activeCategory,
      searchKeyword,
      sortBy,
      stockFilter,
      pagination,
      filteredProducts,
      handleCategoryChange,
      handleSearch,
      handleSort,
      handleStockFilter,
      handleSizeChange,
      handleCurrentChange,
      handleViewDetail,
      handleQuickExchange,
      handleExchange
    }
  }
}
</script>

<style scoped>
.mall {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-display {
  color: #666;
}

.points-value {
  color: #409EFF;
  font-weight: bold;
  font-size: 18px;
}

.category-section {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
  background: white;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #c0c4cc;
}

.low-stock-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #f56c6c;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.product-info {
  padding: 15px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  color: #606266;
  font-size: 14px;
  margin: 0 0 15px 0;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-points {
  display: flex;
  align-items: baseline;
}

.points-required {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.points-unit {
  color: #909399;
  font-size: 14px;
  margin-left: 4px;
}

.product-stats {
  color: #909399;
  font-size: 12px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.no-data {
  text-align: center;
  padding: 40px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.product-detail {
  padding: 20px 0;
}

.detail-image {
  text-align: center;
}

.detail-image img {
  max-width: 100%;
  border-radius: 8px;
}

.no-image-large {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  border-radius: 8px;
}

.detail-info h2 {
  margin: 0 0 20px 0;
  color: #303133;
}

.detail-points {
  margin-bottom: 15px;
}

.points-large {
  color: #f56c6c;
  font-size: 32px;
  font-weight: bold;
}

.detail-stock {
  color: #606266;
  margin-bottom: 10px;
}

.low-stock-text {
  color: #f56c6c;
  font-weight: bold;
}

.detail-stats {
  color: #909399;
  margin-bottom: 20px;
}

.detail-description h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.detail-description p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.detail-actions {
  margin-top: 30px;
}
</style>
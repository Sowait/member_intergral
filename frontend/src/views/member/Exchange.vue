<template>
  <div class="exchange">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分兑换</span>
          <div class="points-display">
            可用积分：<span class="points-value">{{ memberInfo.member?.availablePoints || 0 }}</span>
          </div>
        </div>
      </template>

      <!-- 商品列表 -->
      <div class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-image">
            <img 
              v-if="product.imageUrl" 
              :src="product.imageUrl" 
              :alt="product.productName"
              @error="handleImageError"
            />
            <div v-else class="no-image">
              <img src="/default-product.svg" :alt="product.productName" />
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.productName }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-points">
              <span class="points-required">{{ product.pointsRequired }}</span>
              <span class="points-unit">积分</span>
            </div>
            <div class="product-stock">
              库存：{{ product.stockQuantity }}
            </div>
            <el-button 
              type="primary" 
              @click="handleExchange(product)"
              :disabled="product.stockQuantity <= 0 || (memberInfo.member?.availablePoints || 0) < product.pointsRequired"
              :loading="exchangingProductId === product.id"
            >
              {{ product.stockQuantity <= 0 ? '已售罄' : (memberInfo.member?.availablePoints || 0) < product.pointsRequired ? '积分不足' : '立即兑换' }}
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="products.length === 0" class="no-data">
        暂无可兑换商品
      </div>
    </el-card>

    <!-- 兑换记录 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的兑换记录</span>
          <el-button type="text" @click="$router.push('/member/exchange-records')">查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="exchangeRecords" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" width="200">
          <template #default="scope">
            {{ getProductInfo(scope.row.productId)?.productName }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsUsed" label="使用积分" width="120">
          <template #default="scope">
            <span class="points-used">-{{ scope.row.pointsUsed }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receiveInfo" label="领取信息" />
        <el-table-column prop="exchangeTime" label="兑换时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.exchangeTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div v-if="exchangeRecords.length === 0" class="no-data">
        暂无兑换记录
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'MemberExchange',
  setup() {
    const products = ref([])
    const exchangeRecords = ref([])
    const memberInfo = reactive({ member: {} })
    const exchangingProductId = ref(null)
    const productMap = ref(new Map())

    const loadProducts = async () => {
      try {
        const response = await axios.get('/exchange/products')
        if (response.data.code === 200) {
          products.value = response.data.data
        }
      } catch (error) {
        ElMessage.error('获取商品列表失败')
      }
    }

    const loadExchangeRecords = async () => {
      try {
        const response = await axios.get('/exchange/records')
        if (response.data.code === 200) {
          exchangeRecords.value = response.data.data
          
          // 构建商品映射
          exchangeRecords.value.forEach(record => {
            if (!productMap.value.has(record.productId)) {
              productMap.value.set(record.productId, {
                productName: '商品' + record.productId
              })
            }
          })
        }
      } catch (error) {
        console.error('获取兑换记录失败', error)
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

        exchangingProductId.value = product.id

        const response = await axios.post('/exchange/exchange', {
          productId: product.id
        })

        if (response.data.code === 200) {
          ElMessage.success('兑换成功')
          
          // 刷新数据
          await loadProducts()
          await loadExchangeRecords()
          await loadMemberInfo()
        } else {
          ElMessage.error(response.data.message || '兑换失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.response?.data?.message || '兑换失败')
        }
      } finally {
        exchangingProductId.value = null
      }
    }

    const getProductInfo = (productId) => {
      return productMap.value.get(productId) || { productName: '未知商品' }
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    const handleImageError = (event) => {
      // 当商品图片加载失败时，替换为默认图片
      event.target.src = '/default-product.svg'
    }

    onMounted(() => {
      loadProducts()
      loadExchangeRecords()
      loadMemberInfo()
    })

    return {
      products,
      exchangeRecords,
      memberInfo,
      exchangingProductId,
      handleExchange,
      getProductInfo,
      formatTime,
      handleImageError
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

.points-display {
  color: #666;
}

.points-value {
  color: #409EFF;
  font-weight: bold;
  font-size: 18px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #c0c4cc;
}

.product-info {
  padding: 15px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
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

.product-points {
  margin-bottom: 10px;
}

.points-required {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.points-unit {
  color: #909399;
  font-size: 14px;
}

.product-stock {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.points-used {
  color: #f56c6c;
  font-weight: bold;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 40px;
}
</style>
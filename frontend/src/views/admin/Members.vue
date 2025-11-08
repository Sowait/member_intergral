<template>
  <div class="members">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
          <el-button type="primary" @click="handleExport">导出数据</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="会员号">
            <el-input v-model="filterForm.memberCode" placeholder="请输入会员号" clearable />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="等级">
            <el-select 
              v-model="filterForm.level" 
              placeholder="全部等级" 
              clearable
              style="width: 150px"
            >
              <el-option label="青铜会员" value="BRONZE" />
              <el-option label="白银会员" value="SILVER" />
              <el-option label="黄金会员" value="GOLD" />
              <el-option label="铂金会员" value="PLATINUM" />
            </el-select>
          </el-form-item>
          <el-form-item label="注册时间">
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
            <el-button type="primary" @click="loadMembers">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 会员列表 -->
      <el-table :data="members" style="width: 100%" :row-class-name="'member-row'">
        <el-table-column prop="memberCode" label="会员号" width="220" />
        <el-table-column prop="username" label="用户名" width="110">
          <template #default="scope">
            {{ scope.row.user?.username }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120">
          <template #default="scope">
            {{ scope.row.user?.phone }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="80">
          <template #default="scope">
            <div class="email-cell">{{ scope.row.user?.email }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="90">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)" size="small">
              {{ getLevelText(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalPoints" label="总积分" width="90" />
        <el-table-column prop="availablePoints" label="可用积分" width="100" />
        <el-table-column prop="registerTime" label="注册时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(scope.row)">
                详情
              </el-button>
              <el-button type="warning" size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button type="success" size="small" @click="handleViewPoints(scope.row)">
                积分
              </el-button>
            </div>
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

    <!-- 会员详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="会员详情" width="80%">
      <div v-if="currentMember" class="member-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>基本信息</template>
              <div class="info-item">
                <span class="label">会员号：</span>
                <span class="value">{{ currentMember.memberCode }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ currentMember.user?.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ currentMember.user?.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ currentMember.user?.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">等级：</span>
                <el-tag :type="getLevelTagType(currentMember.level)">
                  {{ getLevelText(currentMember.level) }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>积分信息</template>
              <div class="info-item">
                <span class="label">总积分：</span>
                <span class="value">{{ currentMember.totalPoints }}</span>
              </div>
              <div class="info-item">
                <span class="label">可用积分：</span>
                <span class="value">{{ currentMember.availablePoints }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ formatTime(currentMember.registerTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">最后更新：</span>
                <span class="value">{{ formatTime(currentMember.updateTime) }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 编辑会员对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑会员" width="50%">
      <el-form v-if="editForm" :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="会员号" prop="memberCode">
          <el-input v-model="editForm.memberCode" disabled />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="editForm.level" placeholder="请选择等级">
            <el-option label="青铜会员" value="BRONZE" />
            <el-option label="白银会员" value="SILVER" />
            <el-option label="黄金会员" value="GOLD" />
            <el-option label="铂金会员" value="PLATINUM" />
          </el-select>
        </el-form-item>
        <el-form-item label="总积分" prop="totalPoints">
          <el-input-number v-model="editForm.totalPoints" :min="0" />
        </el-form-item>
        <el-form-item label="可用积分" prop="availablePoints">
          <el-input-number v-model="editForm.availablePoints" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'AdminMembers',
  setup() {
    const members = ref([])
    const currentMember = ref(null)
    const editForm = ref(null)
    const editFormRef = ref()
    const detailDialogVisible = ref(false)
    const editDialogVisible = ref(false)

    const filterForm = reactive({
      memberCode: '',
      username: '',
      level: '',
      dateRange: []
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const editRules = {
      level: [
        { required: true, message: '请选择会员等级', trigger: 'change' }
      ],
      totalPoints: [
        { required: true, message: '请输入总积分', trigger: 'blur' }
      ],
      availablePoints: [
        { required: true, message: '请输入可用积分', trigger: 'blur' }
      ]
    }

    const loadMembers = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size
        }

        // 只有当有输入内容时才添加keyword参数
        if (filterForm.memberCode && filterForm.memberCode.trim()) {
          params.keyword = filterForm.memberCode.trim()
        } else if (filterForm.username && filterForm.username.trim()) {
          params.keyword = filterForm.username.trim()
        }

        if (filterForm.level) {
          params.level = filterForm.level
        }

        if (filterForm.dateRange && filterForm.dateRange.length === 2) {
          params.startDate = filterForm.dateRange[0]
          params.endDate = filterForm.dateRange[1]
        }

        console.log('发送的查询参数:', params)
        const response = await axios.get('/member/list', { params })
        if (response.data.code === 200) {
          members.value = response.data.data.records || []
          pagination.total = response.data.data.total || 0
          console.log('返回的会员数据:', members.value)
        }
      } catch (error) {
        console.error('获取会员列表失败:', error)
        ElMessage.error('获取会员列表失败')
      }
    }

    const resetFilter = () => {
      filterForm.memberCode = ''
      filterForm.username = ''
      filterForm.level = ''
      filterForm.dateRange = []
      pagination.current = 1
      loadMembers()
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      loadMembers()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadMembers()
    }

    const handleView = async (member) => {
      try {
        const response = await axios.get(`/member/${member.id}`)
        if (response.data.code === 200) {
          currentMember.value = response.data.data
          detailDialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取会员详情失败')
      }
    }

    const handleEdit = (member) => {
      editForm.value = { ...member }
      editDialogVisible.value = true
    }

    const handleSaveEdit = async () => {
      try {
        await editFormRef.value.validate()
        
        const response = await axios.put(`/member/${editForm.value.id}`, editForm.value)
        if (response.data.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadMembers()
        } else {
          ElMessage.error(response.data.message || '更新失败')
        }
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }

    const handleViewPoints = () => {
      // 可以跳转到积分记录页面，或者打开对话框显示
      ElMessage.info('积分记录功能待实现')
    }

    const handleExport = async () => {
      try {
        await ElMessageBox.confirm('确定要导出会员数据吗？', '确认导出', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        ElMessage.success('导出功能待实现')
      } catch (error) {
        // 用户取消
      }
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

    const handleLevelChange = (value) => {
      console.log('等级选择变化:', value)
      // 可以在这里添加额外的处理逻辑，比如自动触发查询
      // loadMembers()
    }

    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    onMounted(() => {
      loadMembers()
    })

    return {
      members,
      currentMember,
      editForm,
      editFormRef,
      detailDialogVisible,
      editDialogVisible,
      filterForm,
      pagination,
      editRules,
      loadMembers,
      resetFilter,
      handleSizeChange,
      handleCurrentChange,
      handleView,
      handleEdit,
      handleSaveEdit,
      handleViewPoints,
      handleExport,
      handleLevelChange,
      getLevelText,
      getLevelTagType,
      formatTime
    }
  }
}
</script>

<style scoped>
.members {
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

.pagination {
  margin-top: 20px;
  text-align: center;
}

.member-detail {
  padding: 20px 0;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.label {
  width: 100px;
  color: #606266;
  font-weight: 500;
}

.value {
  color: #303133;
  flex: 1;
}

/* 会员列表布局优化 */
.member-row .el-table__cell {
  padding: 8px 0;
}

.email-cell {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  margin-left: 0;
  padding: 5px 8px;
  font-size: 12px;
}
</style>
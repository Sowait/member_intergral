<template>
  <div class="profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人资料</span>
          <el-button type="primary" @click="handleEdit">编辑资料</el-button>
        </div>
      </template>

      <div class="profile-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="avatar-section">
              <el-avatar :size="120" icon="User" />
              <h3>{{ memberInfo.user?.username }}</h3>
              <el-tag :type="getLevelTagType(memberInfo.member?.level)">
                {{ getLevelText(memberInfo.member?.level) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="info-section">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="会员号">{{ memberInfo.member?.memberCode }}</el-descriptions-item>
                <el-descriptions-item label="用户名">{{ memberInfo.user?.username }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ memberInfo.user?.phone }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ memberInfo.user?.email }}</el-descriptions-item>
                <el-descriptions-item label="总积分">{{ memberInfo.member?.totalPoints }}</el-descriptions-item>
                <el-descriptions-item label="可用积分">{{ memberInfo.member?.availablePoints }}</el-descriptions-item>
                <el-descriptions-item label="注册时间" :span="2">{{ formatTime(memberInfo.member?.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="最后更新" :span="2">{{ formatTime(memberInfo.member?.updateTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 修改密码 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>修改密码</span>
      </template>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="max-width: 500px;">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="50%">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
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
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'MemberProfile',
  setup() {
    const memberInfo = ref({})
    const editDialogVisible = ref(false)
    const passwordLoading = ref(false)
    const passwordFormRef = ref()
    const editFormRef = ref()

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const editForm = reactive({
      username: '',
      phone: '',
      email: ''
    })

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const editRules = {
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    }

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

    const handleEdit = () => {
      editForm.username = memberInfo.value.user?.username
      editForm.phone = memberInfo.value.user?.phone
      editForm.email = memberInfo.value.user?.email
      editDialogVisible.value = true
    }

    const handleSaveEdit = async () => {
      try {
        await editFormRef.value.validate()
        
        const response = await axios.put('/member/profile', {
          phone: editForm.phone,
          email: editForm.email
        })
        
        if (response.data.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadMemberInfo()
        } else {
          ElMessage.error(response.data.message || '更新失败')
        }
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }

    const handleChangePassword = async () => {
      try {
        await passwordFormRef.value.validate()
        passwordLoading.value = true

        const response = await axios.post('/auth/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })

        if (response.data.code === 200) {
          ElMessage.success('密码修改成功')
          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          ElMessage.error(response.data.message || '密码修改失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      } finally {
        passwordLoading.value = false
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
      loadMemberInfo()
    })

    return {
      memberInfo,
      editDialogVisible,
      passwordLoading,
      passwordForm,
      editForm,
      passwordFormRef,
      editFormRef,
      passwordRules,
      editRules,
      handleEdit,
      handleSaveEdit,
      handleChangePassword,
      getLevelText,
      getLevelTagType,
      formatTime
    }
  }
}
</script>

<style scoped>
.profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  padding: 20px 0;
}

.avatar-section {
  text-align: center;
}

.avatar-section h3 {
  margin: 20px 0 10px 0;
  color: #303133;
}

.info-section {
  padding: 0 20px;
}
</style>
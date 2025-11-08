import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/member',
    name: 'Member',
    component: () => import('../layouts/MemberLayout.vue'),
    redirect: '/member/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'MemberDashboard',
        component: () => import('../views/member/Dashboard.vue')
      },
      {
        path: 'profile',
        name: 'MemberProfile',
        component: () => import('../views/member/Profile.vue')
      },
      {
        path: 'points',
        name: 'MemberPoints',
        component: () => import('../views/member/Points.vue')
      },
      {
        path: 'mall',
        name: 'MemberMall',
        component: () => import('../views/member/Mall.vue')
      },
      {
        path: 'exchange',
        name: 'MemberExchange',
        component: () => import('../views/member/Exchange.vue')
      },
      {
        path: 'orders',
        name: 'MemberOrders',
        component: () => import('../views/member/Orders.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'members',
        name: 'AdminMembers',
        component: () => import('../views/admin/Members.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/Orders.vue')
      },
      {
        path: 'exchange',
        name: 'AdminExchange',
        component: () => import('../views/admin/Exchange.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
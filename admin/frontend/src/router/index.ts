import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';
import { hasAuthToken } from '@/lib/auth';

const DashboardView = () => import('@/views/DashboardView.vue');
const AppUsersView = () => import('@/views/AppUsersView.vue');
const FamilyBindingsView = () => import('@/views/FamilyBindingsView.vue');
const SharedLogsView = () => import('@/views/SharedLogsView.vue');
const MedicineProfilesView = () => import('@/views/MedicineProfilesView.vue');
const LoginView = () => import('@/views/LoginView.vue');

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true }
  },
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView
      },
      {
        path: 'app-users',
        name: 'app-users',
        component: AppUsersView
      },
      {
        path: 'family-bindings',
        name: 'family-bindings',
        component: FamilyBindingsView
      },
      {
        path: 'shared-logs',
        name: 'shared-logs',
        component: SharedLogsView
      },
      {
        path: 'medicine-profiles',
        name: 'medicine-profiles',
        component: MedicineProfilesView
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.beforeEach((to) => {
  const authed = hasAuthToken();

  if (to.matched.some((record) => record.meta.requiresAuth) && !authed) {
    return {
      path: '/login',
      query: to.fullPath === '/' ? undefined : { redirect: to.fullPath }
    };
  }

  if (to.meta.guestOnly && authed) {
    return { path: '/dashboard' };
  }

  return true;
});

export default router;

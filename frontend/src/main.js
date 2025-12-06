import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './common-styles.css'

// 暂时注释Font Awesome相关代码
// import { library } from '@fortawesome/fontawesome-svg-core'
// import { fas } from '@fortawesome/free-solid-svg-icons'
// import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// library.add(fas)
// Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.config.productionTip = false

store.dispatch('loadUserProgress')

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
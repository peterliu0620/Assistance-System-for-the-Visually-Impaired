import Vue from 'vue';
import App from './App.vue';
import './uni.promisify.adaptor';

Vue.config.productionTip = false;
(App as typeof Vue & { mpType?: string }).mpType = 'app';

const app = new Vue({
	...App
});

app.$mount();

<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	page {
		--bg-0: #070b12;
		--bg-1: #0d1420;
		--card: rgba(14, 22, 36, 0.78);
		--card-border: rgba(123, 164, 218, 0.32);
		--text-main: #edf4ff;
		--text-sub: #9eb4ce;
		--brand: #5ec8ff;
		--brand-strong: #3caef5;
		--accent: #ffd166;
		--danger: #ff6b6b;
		--radius-lg: 22rpx;
		--shadow-lg: 0 18rpx 42rpx rgba(4, 10, 20, 0.55);
		font-family: "Avenir Next", "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
		background: radial-gradient(120% 100% at 10% 0%, #13223a 0%, var(--bg-0) 62%);
		color: var(--text-main);
	}

	button {
		border: none;
		border-radius: 14rpx;
		box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.24);
	}
</style>

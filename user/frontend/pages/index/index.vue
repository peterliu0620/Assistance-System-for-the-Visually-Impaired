<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="card">
			<text class="title">视障智能视觉辅助</text>
			<text class="desc">说出“这是什么？”，系统将拍照识别并语音播报</text>

			<view class="gesture-zone" @tap="onGestureTap" @longpress="onGestureLongPress">
				<text class="gesture-title">手势操作区</text>
				<text class="gesture-text">单击：{{ gestureActionLabel(settings.gestureSingleTap) }}</text>
				<text class="gesture-text">双击：{{ gestureActionLabel(settings.gestureDoubleTap) }}</text>
				<text class="gesture-text">长按：{{ gestureActionLabel(settings.gestureLongPress) }}</text>
			</view>

			<view class="result" v-if="result">
				<text class="result-title">结构化播报结果</text>
				<text class="line">触发指令：{{ result.triggerCommand }}</text>
				<text class="line">场景：{{ result.scene }}</text>
				<text class="line">说明：{{ result.positionSummary }}</text>
				<text class="line">播报：{{ result.narration }}</text>
			</view>

			<view class="debug">
				<view class="debug-head">
					<text class="debug-title">调试信息</text>
					<button class="tiny" @click="clearDebugLogs">清空</button>
				</view>
				<text class="debug-line">API: {{ apiBase }}</text>
				<text class="debug-line">状态: {{ debug.status }}</text>
				<text class="debug-line">最后指令: {{ debug.lastCommand || '-' }}</text>
				<text class="debug-line">最后图片: {{ debug.lastImagePath || '-' }}</text>
				<text class="debug-line">最后响应码: {{ debug.lastStatusCode || '-' }}</text>
				<text class="debug-line">最后错误: {{ debug.lastError || '-' }}</text>
				<text class="debug-log" v-for="(log, idx) in debug.logs" :key="idx">{{ log }}</text>
			</view>
		</view>

		<view class="bottom-actions">
			<button class="action-btn primary" @click="startVoiceTrigger" :disabled="loading">
				{{ loading ? '识别中' : '语音识别' }}
			</button>
			<button class="action-btn" @click="scanByDefaultCommand" :disabled="loading">直接拍照</button>
			<button class="action-btn" @click="goUserCenter">用户中心</button>
		</view>
	</view>
</template>

<script>
	import { loadUserSettings } from '../../utils/user-settings.js'

	export default {
		data() {
			return {
				loading: false,
				result: null,
				apiBase: 'http://10.135.102.177:8080',
				audioPlayer: null,
				singleTapTimer: null,
				settings: loadUserSettings(),
				debug: {
					status: 'idle',
					lastCommand: '',
					lastImagePath: '',
					lastStatusCode: '',
					lastError: '',
					logs: []
				}
			}
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold'
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : ''
			}
		},
		onLoad() {
			this.audioPlayer = uni.createInnerAudioContext()
			this.audioPlayer.autoplay = true
			this.audioPlayer.onPlay(() => {
				this.debug.status = 'spoken-cloud-playing'
				this.pushDebugLog('云端音频开始播放')
			})
			this.audioPlayer.onEnded(() => {
				this.debug.status = 'spoken-cloud-ended'
				this.pushDebugLog('云端音频播放结束')
			})
			this.audioPlayer.onError((err) => {
				this.debug.status = 'spoken-cloud-failed'
				this.setDebugError(`云端音频播放失败: ${JSON.stringify(err)}`)
			})
			this.ensureLoggedIn()
			this.refreshSettings()
		},
		onShow() {
			this.ensureLoggedIn()
			this.refreshSettings()
		},
		onUnload() {
			if (this.audioPlayer) {
				this.audioPlayer.destroy()
				this.audioPlayer = null
			}
			if (this.singleTapTimer) {
				clearTimeout(this.singleTapTimer)
				this.singleTapTimer = null
			}
		},
		methods: {
			refreshSettings() {
				this.settings = loadUserSettings()
			},
			ensureLoggedIn() {
				const user = uni.getStorageSync('auth_user')
				if (!user || !user.id) {
					uni.reLaunch({
						url: '/pages/auth/auth'
					})
					return false
				}
				return true
			},
			gestureActionLabel(action) {
				if (action === 'voice_trigger') return '语音触发识别'
				if (action === 'direct_scan') return '直接拍照识别'
				if (action === 'open_user_center') return '打开用户中心'
				return '无动作'
			},
			onGestureTap() {
				if (this.singleTapTimer) {
					clearTimeout(this.singleTapTimer)
					this.singleTapTimer = null
					this.executeGestureAction(this.settings.gestureDoubleTap)
					return
				}
				this.singleTapTimer = setTimeout(() => {
					this.executeGestureAction(this.settings.gestureSingleTap)
					this.singleTapTimer = null
				}, 260)
			},
			onGestureLongPress() {
				if (this.singleTapTimer) {
					clearTimeout(this.singleTapTimer)
					this.singleTapTimer = null
				}
				this.executeGestureAction(this.settings.gestureLongPress)
			},
			executeGestureAction(action) {
				if (!this.ensureLoggedIn()) {
					return
				}
				this.triggerHaptic()
				if (action === 'voice_trigger') {
					this.startVoiceTrigger()
					return
				}
				if (action === 'direct_scan') {
					this.scanByDefaultCommand()
					return
				}
				if (action === 'open_user_center') {
					this.goUserCenter()
					return
				}
				this.pushDebugLog('手势动作：无操作')
			},
			triggerHaptic() {
				if (this.settings.hapticLevel === 1) {
					uni.vibrateShort()
					return
				}
				if (this.settings.hapticLevel === 2) {
					uni.vibrateShort()
					setTimeout(() => uni.vibrateShort(), 180)
					return
				}
				uni.vibrateLong()
			},
			pushDebugLog(message) {
				const now = new Date()
				const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
				const line = `[${time}] ${message}`
				this.debug.logs.unshift(line)
				console.log(`[VISION-DEBUG] ${line}`)
				if (this.debug.logs.length > 10) {
					this.debug.logs.pop()
				}
			},
			setDebugError(message) {
				this.debug.lastError = message
				console.error(`[VISION-DEBUG][ERROR] ${message}`)
				this.pushDebugLog(`ERROR: ${message}`)
			},
			clearDebugLogs() {
				this.debug.logs = []
				this.debug.lastError = ''
				this.pushDebugLog('已清空调试日志')
			},
			startVoiceTrigger() {
				if (!this.ensureLoggedIn()) {
					return
				}
				if (this.loading) {
					return
				}
				this.triggerHaptic()
				const command = '这是什么？'
				this.debug.status = 'voice-triggered'
				this.debug.lastCommand = command
				this.pushDebugLog('语音触发识别：先播报提示，再自动拍照')
				this.playGuideThenCapture(command)
			},
			playGuideThenCapture(command) {
				const guide = '即将开始识别，请将目标放入画面后拍照。'
				let spoken = false
				if (typeof plus !== 'undefined') {
					if (plus.speech && typeof plus.speech.speak === 'function') {
						plus.speech.speak(guide)
						spoken = true
					} else if (plus.tts && typeof plus.tts.speak === 'function') {
						plus.tts.speak(guide)
						spoken = true
					}
				} else if (typeof window !== 'undefined' && window.speechSynthesis) {
					const utterance = new SpeechSynthesisUtterance(guide)
					utterance.lang = 'zh-CN'
					window.speechSynthesis.speak(utterance)
					spoken = true
				}

				if (!spoken) {
					uni.showToast({ title: '即将打开相机，请拍照', icon: 'none' })
					this.pushDebugLog('本地提示音不可用，使用文字提示')
				} else {
					this.pushDebugLog('已播报拍照引导语')
				}

				setTimeout(() => {
					this.pickAndAnalyzeImage(command)
				}, 600)
			},
			handleVoiceCommand(command) {
				const normalized = (command || '').trim()
				if (!normalized) {
					this.setDebugError('未识别到有效语音指令')
					uni.showToast({ title: '未识别到指令', icon: 'none' })
					return
				}
				this.debug.lastCommand = normalized
				this.debug.status = 'command-ready'
				this.pushDebugLog(`收到指令: ${normalized}`)

				if (normalized.indexOf('什么') === -1 && normalized.indexOf('这') === -1) {
					uni.showToast({ title: `当前指令：${normalized}` , icon: 'none' })
				}

				this.pickAndAnalyzeImage(normalized)
			},
			scanByDefaultCommand() {
				if (!this.ensureLoggedIn()) {
					return
				}
				this.triggerHaptic()
				this.pickAndAnalyzeImage('这是什么？')
			},
			pickAndAnalyzeImage(command) {
				this.loading = true
				this.debug.status = 'camera-opening'
				this.pushDebugLog('打开相机拍照')
				uni.chooseImage({
					count: 1,
					sourceType: ['camera'],
					success: (chooseRes) => {
						const imagePath = chooseRes.tempFilePaths[0]
						this.debug.lastImagePath = imagePath || ''
						this.debug.status = 'image-picked'
						this.pushDebugLog(`拍照成功: ${imagePath}`)
						this.uploadForAnalyze(imagePath, command)
					},
					fail: () => {
						this.loading = false
						this.debug.status = 'camera-failed'
						this.setDebugError('未获取到照片')
						uni.showToast({ title: '未获取到照片', icon: 'none' })
					}
				})
			},
			uploadForAnalyze(imagePath, command) {
				this.debug.status = 'uploading'
				this.pushDebugLog(`开始上传: ${this.apiBase}/api/vision/analyze`)
				uni.uploadFile({
					url: `${this.apiBase}/api/vision/analyze`,
					filePath: imagePath,
					name: 'image',
					formData: {
						command
					},
					success: (uploadRes) => {
						this.loading = false
						this.debug.lastStatusCode = uploadRes.statusCode
						this.pushDebugLog(`上传完成，HTTP ${uploadRes.statusCode}`)
						if (uploadRes.statusCode !== 200) {
							let detail = uploadRes.data
							try {
								const errorJson = JSON.parse(uploadRes.data || '{}')
								detail = errorJson.message || JSON.stringify(errorJson)
							} catch (e) {
								// keep raw detail
							}
							this.debug.status = 'analyze-failed'
							this.setDebugError(`识别请求失败: HTTP ${uploadRes.statusCode} ${detail || ''}`)
							uni.showToast({ title: '识别失败，请查看调试信息', icon: 'none' })
							return
						}
						let data = null
						try {
							data = JSON.parse(uploadRes.data)
						} catch (e) {
							this.debug.status = 'parse-failed'
							this.setDebugError(`返回数据解析失败: ${e && e.message ? e.message : 'unknown'}`)
							uni.showToast({ title: '返回数据解析失败', icon: 'none' })
							return
						}

						this.result = data
						this.debug.status = 'analyzed'
						this.pushDebugLog('返回解析成功，准备语音播报')
						try {
							this.speak(this.pickNarrationText(data))
						} catch (e) {
							this.debug.status = 'speak-failed'
							this.setDebugError(`语音播报失败: ${e && e.message ? e.message : 'unknown'}`)
						}
					},
					fail: () => {
						this.loading = false
						this.debug.status = 'upload-failed'
						this.setDebugError('识别请求失败，请确认后端已启动')
						uni.showToast({ title: '识别请求失败，请确认后端已启动', icon: 'none' })
					}
				})
			},
			pickNarrationText(data) {
				if (!data) {
					return '识别完成'
				}
				if (this.settings.broadcastGranularity === 'concise') {
					const firstItem = data.items && data.items.length ? data.items[0].name : '物体'
					const scene = data.scene || '当前场景'
					return `场景：${scene}。核心物品：${firstItem}。`
				}
				return data.narration || '识别完成'
			},
			speak(text) {
				if (!text) {
					return
				}
				this.pushDebugLog(`开始播报: ${text}`)

				if (typeof plus !== 'undefined') {
					if (plus.speech && typeof plus.speech.speak === 'function') {
						plus.speech.speak(text)
						this.debug.status = 'spoken-app'
						this.pushDebugLog('使用 plus.speech.speak 完成播报')
						return
					}

					if (plus.tts && typeof plus.tts.speak === 'function') {
						plus.tts.speak(text)
						this.debug.status = 'spoken-app'
						this.pushDebugLog('使用 plus.tts.speak 完成播报')
						return
					}

					this.debug.status = 'spoken-cloud-request'
					this.pushDebugLog('当前基座不支持 App TTS，切换云端 TTS')
					this.playCloudTts(text)
					return
				}

				if (typeof window !== 'undefined' && window.speechSynthesis) {
					const utterance = new SpeechSynthesisUtterance(text)
					utterance.lang = 'zh-CN'
					utterance.rate = this.settings.speechRate
					const voices = window.speechSynthesis.getVoices ? window.speechSynthesis.getVoices() : []
					const target = voices.find(v => (v.name || '').toLowerCase().includes(this.settings.voiceTimbre.toLowerCase()))
					if (target) {
						utterance.voice = target
					}
					window.speechSynthesis.speak(utterance)
					this.debug.status = 'spoken-web'
					this.pushDebugLog('使用 Web Speech Synthesis 完成播报')
					return
				}

				this.debug.status = 'spoken-cloud-request'
				this.pushDebugLog('当前环境无本地播报能力，切换云端 TTS')
				this.playCloudTts(text)
			},
			playCloudTts(text) {
				uni.request({
					url: `${this.apiBase}/api/voice/tts`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						text,
						voice: this.settings.voiceTimbre
					},
					success: (res) => {
						if (res.statusCode !== 200 || !res.data || !res.data.audioUrl) {
							const detail = res.data && res.data.message ? res.data.message : JSON.stringify(res.data || {})
							this.setDebugError(`云端TTS请求失败: HTTP ${res.statusCode} ${detail}`)
							return
						}
						const audioUrl = res.data.audioUrl
						this.pushDebugLog(`云端TTS成功，音频地址: ${audioUrl}`)
						this.playFromUrl(audioUrl)
					},
					fail: (err) => {
						this.setDebugError(`云端TTS请求异常: ${JSON.stringify(err)}`)
					}
				})
			},
			playFromUrl(url) {
				if (!this.audioPlayer) {
					this.audioPlayer = uni.createInnerAudioContext()
					this.audioPlayer.autoplay = true
				}
				this.debug.status = 'spoken-cloud-ready'
				this.pushDebugLog('准备播放云端音频')
				this.audioPlayer.src = url
				if (typeof this.audioPlayer.playbackRate !== 'undefined') {
					this.audioPlayer.playbackRate = this.settings.speechRate
				}
				this.audioPlayer.play()
			},
			goAuth() {
				uni.navigateTo({
					url: '/pages/auth/auth'
				})
			},
			goUserCenter() {
				uni.navigateTo({
					url: '/pages/user-center/user-center'
				})
			}
		}
	}
</script>

<style>
	.page {
		min-height: 100vh;
		display: flex;
		align-items: flex-start;
		justify-content: center;
		padding: 28rpx 28rpx 156rpx;
	}

	.theme-gold {
		background: radial-gradient(140% 90% at 20% 0%, #182c45 0%, #090e16 66%);
	}

	.theme-yellow {
		background: radial-gradient(140% 90% at 20% 0%, #2d250d 0%, #0b0b08 66%);
	}

	.card {
		width: 100%;
		max-width: 680rpx;
		border-radius: 20rpx;
		padding: 30rpx;
		box-sizing: border-box;
		backdrop-filter: blur(10rpx);
		box-shadow: var(--shadow-lg);
	}

	.theme-gold .card {
		background: linear-gradient(160deg, rgba(18, 27, 43, 0.86), rgba(8, 15, 24, 0.85));
		border: 2rpx solid rgba(255, 209, 102, 0.38);
	}

	.theme-yellow .card {
		background: linear-gradient(160deg, rgba(23, 20, 10, 0.9), rgba(9, 9, 7, 0.88));
		border: 2rpx solid rgba(255, 230, 0, 0.5);
	}

	.title {
		display: block;
		font-size: 46rpx;
		color: #f4f8ff;
		font-weight: bold;
		letter-spacing: 1rpx;
		margin-bottom: 12rpx;
	}

	.desc {
		display: block;
		font-size: 28rpx;
		color: #a8bfd8;
		line-height: 1.7;
		margin-bottom: 20rpx;
	}

	.btn {
		margin-bottom: 16rpx;
		background: linear-gradient(145deg, #26374d, #1a2738);
		color: #eaf3ff;
		font-size: 28rpx;
	}

	.primary {
		background: linear-gradient(135deg, #62d0ff, #3ba5f5);
		color: #0a1a2a;
		font-weight: bold;
	}

	.result {
		margin-top: 24rpx;
		padding: 18rpx;
		border-radius: 16rpx;
		background: rgba(10, 18, 31, 0.76);
		border: 2rpx solid rgba(108, 151, 205, 0.35);
	}

	.result-title {
		display: block;
		font-size: 30rpx;
		color: #7cd3ff;
		margin-bottom: 10rpx;
	}

	.line {
		display: block;
		font-size: 26rpx;
		color: #d7e5f7;
		line-height: 1.6;
		margin-bottom: 8rpx;
	}

	.debug {
		margin-top: 24rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: rgba(6, 11, 18, 0.76);
		border: 2rpx dashed rgba(116, 161, 220, 0.36);
	}

	.debug-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 10rpx;
	}

	.debug-title {
		font-size: 28rpx;
		color: #8bd3ff;
		font-weight: bold;
	}

	.tiny {
		font-size: 22rpx;
		padding: 0 16rpx;
		height: 52rpx;
		line-height: 52rpx;
		background: linear-gradient(145deg, #283f58, #203247);
		color: #d5ebff;
		margin: 0;
	}

	.debug-line {
		display: block;
		font-size: 22rpx;
		color: #b8d3e8;
		line-height: 1.6;
		margin-bottom: 6rpx;
		word-break: break-all;
	}

	.debug-log {
		display: block;
		font-size: 22rpx;
		color: #93f5c2;
		line-height: 1.5;
		margin-top: 6rpx;
		word-break: break-all;
	}

	.gesture-zone {
		margin-top: 12rpx;
		margin-bottom: 18rpx;
		padding: 16rpx;
		border: 2rpx dashed rgba(109, 159, 226, 0.46);
		border-radius: 14rpx;
		background: rgba(255, 255, 255, 0.04);
	}

	.gesture-title {
		display: block;
		color: #8bd3ff;
		font-size: 26rpx;
		font-weight: bold;
		margin-bottom: 6rpx;
	}

	.gesture-text {
		display: block;
		color: #cde6fb;
		font-size: 22rpx;
		line-height: 1.6;
	}

	.font-large .title {
		font-size: 54rpx;
	}

	.font-large .desc {
		font-size: 34rpx;
	}

	.font-large .line,
	.font-large .debug-line,
	.font-large .debug-log,
	.font-large .gesture-text {
		font-size: 30rpx;
	}

	.bottom-actions {
		position: fixed;
		left: 20rpx;
		right: 20rpx;
		bottom: 18rpx;
		display: flex;
		gap: 12rpx;
		padding: 14rpx;
		border-radius: 18rpx;
		background: rgba(8, 13, 21, 0.92);
		border: 1px solid rgba(112, 150, 194, 0.42);
		box-shadow: 0 14rpx 30rpx rgba(0, 0, 0, 0.45);
		z-index: 30;
	}

	.action-btn {
		flex: 1;
		height: 78rpx;
		line-height: 78rpx;
		font-size: 24rpx;
		margin: 0;
		padding: 0;
		background: linear-gradient(145deg, #2a3d53, #1f2e40);
		color: #eef6ff;
		border-radius: 12rpx;
	}

	.action-btn.primary {
		background: linear-gradient(135deg, #62d0ff, #3caef5);
		color: #081a2c;
		font-weight: bold;
	}
</style>

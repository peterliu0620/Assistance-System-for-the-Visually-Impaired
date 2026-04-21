uni.addInterceptor({
  returnValue(result) {
    if (!(result && (typeof result === 'object' || typeof result === 'function') && typeof result.then === 'function')) {
      return result
    }

    return new Promise((resolve, reject) => {
      result.then((response) => {
        if (!response || !Array.isArray(response)) {
          resolve(response)
          return
        }

        const [error, data] = response
        if (error) {
          reject(error)
          return
        }

        resolve(data)
      }, reject)
    })
  }
})

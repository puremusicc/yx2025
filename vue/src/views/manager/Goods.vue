<template>
  <div>
    <div class="search">
      <el-input placeholder="请输入名称" style="width: 200px; margin-left: 10px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="operation">
      <el-button v-if="user.role === 'ADMIN' || user.role === 'BUSINESS'" type="primary" plain @click="handleAdd">新增</el-button>
      <el-button v-if="user.role === 'ADMIN' || user.role === 'BUSINESS'" type="danger" plain @click="delBatch">批量删除</el-button>
      <el-button v-if="user.role === 'ADMIN'" type="danger" plain @click="exportData">批量导出</el-button>
      <el-button v-if="user.role === 'USER'" type="success" plain @click="addToCartBatch">加入购物车</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" strip @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="price" label="价格"></el-table-column>
        <el-table-column prop="discount" label="折扣"></el-table-column>
        <el-table-column prop="img" label="图片">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 40px; height: 40px;" v-if="scope.row.img"
                        :src="scope.row.img" :preview-src-list="[scope.row.img]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="descr" label="描述"></el-table-column>
        <el-table-column prop="origin" label="原材料"></el-table-column>
        <el-table-column prop="taste" label="口味"></el-table-column>
        <el-table-column prop="specs" label="规格"></el-table-column>
        <el-table-column prop="date" label="上架日期"></el-table-column>
        <el-table-column prop="status" label="上架状态">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.status === '上架'">上架</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '下架'">下架</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessName" label="商家"></el-table-column>
        <el-table-column prop="categoryName" label="分类"></el-table-column>
        <el-table-column v-if="user.role === 'ADMIN' || user.role === 'BUSINESS'" label="操作" align="center" width="180">
          <template v-slot="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </div>


    <el-dialog title="商品" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="名称"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" placeholder="价格"></el-input>
        </el-form-item>
        <el-form-item label="折扣" prop="discount">
          <el-input v-model="form.discount" placeholder="折扣: 1表示不打折，折扣可以设置小数"></el-input>
        </el-form-item>
        <el-form-item label="图片" prop="img">
          <el-upload
              :action="$baseUrl + '/files/upload'"
              :headers="{ token: user.token }"
              :on-success="handleFileSuccess"
          >
            <el-button type="primary">上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="descr">
          <el-input v-model="form.descr" placeholder="描述"></el-input>
        </el-form-item>
        <el-form-item label="原材料" prop="origin">
          <el-input v-model="form.origin" placeholder="原材料"></el-input>
        </el-form-item>
        <el-form-item label="口味" prop="taste">
          <el-input v-model="form.taste" placeholder="口味"></el-input>
        </el-form-item>
        <el-form-item label="规格" prop="specs">
          <el-input v-model="form.specs" placeholder="规格"></el-input>
        </el-form-item>
        <el-form-item label="上架日期" prop="date">
          <el-date-picker format="yyyy-MM-dd" value-format="yyyy-MM-dd"
                          v-model="form.date" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="上架状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="上架"></el-radio>
            <el-radio label="下架"></el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select style="width: 100%" v-model="form.categoryId" @change="$forceUpdate()">
            <el-option v-for="item in categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

  、<el-dialog title="我的购物车" :visible.sync="cartVisible" width="60%">
    <el-table :data="cartItems" border stripe>
      <el-table-column prop="name" label="商品名称"></el-table-column>
      <el-table-column prop="businessName" label="商家"></el-table-column>
      <el-table-column prop="price" label="单价"></el-table-column>
      <el-table-column label="数量">
        <template slot-scope="scope">
          <el-input-number v-model="scope.row.quantity" :min="1"></el-input-number>
        </template>
      </el-table-column>
      <el-table-column label="小计">
        <template slot-scope="scope">
          {{ (scope.row.price * scope.row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="removeFromCart(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-form :model="orderForm" label-width="100px" style="margin-top: 20px">
      <el-form-item label="收货地址">
        <el-input v-model="orderForm.address"></el-input>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="orderForm.phone"></el-input>
      </el-form-item>
      <el-form-item label="订单备注">
        <el-input v-model="orderForm.comment" type="textarea"></el-input>
      </el-form-item>
    </el-form>

    <div style="margin-top: 20px; text-align: right">
      <div style="margin-bottom: 15px; font-size: 16px">
        总计: {{ calculateTotal().toFixed(2) }} 元
      </div>
      <el-button type="primary" @click="showPaymentDialog">提交订单</el-button>
    </div>
  </el-dialog>

    <!-- 支付方式选择对话框 -->
    <el-dialog title="选择支付方式" :visible.sync="paymentVisible" width="30%">
      <el-radio-group v-model="paymentType">
        <el-radio label="支付宝支付"></el-radio>
        <el-radio label="微信支付"></el-radio>
        <el-radio label="银行卡支付"></el-radio>
      </el-radio-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="paymentVisible = false">取消</el-button>
        <el-button type="primary" @click="createOrder">确认支付</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "Goods",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      name:null,
      fromVisible: false,
      categoryList:[],
      cartVisible: false,  // 控制购物车弹窗显示
      cartItems: [],       // 购物车商品列表
      // ...其他数据...
      orderForm: {
        address: '',
        phone: '',
        comment: ''
      },
      paymentVisible: false,//支付弹框
      paymentType: '支付宝支付',
      form: {
        categoryId:''
      },
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      // 校验规则
      rules: {
        name: [
          {required: true, message: '请输入名称', trigger: 'blur'},
        ],
        price: [
          {required: true, message: '请输入价格', trigger: 'blur'},
        ],

      },

      ids: []
    }
  },
  created() {
    this.load(1)

    let businessId=(this.user.role==='BUSINESS') ? this.user.id : null
    this.$request.get('/category/selectAll', {
      params: {
        businessId:businessId
      }
    }).then(res => {
      this.categoryList = res.data || []
    })

  },
  methods: {
    handleAdd() {   // 新增数据
      this.form = {}  // 新增数据的时候清空数据
      this.fromVisible = true   // 打开弹窗
    },
    handleEdit(row) {   // 编辑数据
      this.form = JSON.parse(JSON.stringify(row))  // 给form对象赋值  注意要深拷贝数据
      this.fromVisible = true   // 打开弹窗
    },
    save() {   // 保存按钮触发的逻辑  它会触发新增或者更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/goods/update' : '/goods/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // 表示成功保存
              this.$message.success('保存成功')
              this.load(1)
              this.fromVisible = false
            } else {
              this.$message.error(res.msg)  // 弹出错误的信息
            }
          })
        }
      })
    },
    exportData(){//批量导出
      if(!this.ids.length){//没有选择行时全部导出
        window.open( this.$baseUrl+'/goods/export?token='+this.user.token)
      }
      else {  // 否则，导出选中的数据
        window.open('/goods/export?token=' + this.user.token + '&ids=' + this.ids.join(','))
      }
    },

    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/goods/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.$confirm('您确定批量删除这些数据吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/goods/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    // 批量加入购物车
    addToCartBatch() {
      if (!this.ids.length) {
        this.$message.warning('请选择商品')
        return
      }
      // 获取选中的商品数据
      const idSet = new Set(this.ids)
      const selectedGoods = this.tableData.filter(item => idSet.has(item.id))
      this.cartItems.push(...selectedGoods)  // 添加到购物车
      this.$message.success('已添加到购物车')
      this.cartVisible = true  // 自动打开购物车弹窗
    },

    // 从购物车移除商品
    removeFromCart(id) {
      this.cartItems = this.cartItems.filter(item => item.id !== id)
    },
    // 显示支付对话框
    showPaymentDialog() {
      if (!this.cartItems.length) {
        this.$message.warning('购物车为空，请先添加商品')
        return
      }
      if (!this.orderForm.address || !this.orderForm.phone) {
        this.$message.warning('请填写收货地址和联系电话')
        return
      }
      this.paymentVisible = true
    },
    // 创建订单
    async createOrder() {
      try {
        // 1. 获取商品详细信息
        const goodsDetails = await this.$request({
          url: '/goods/selectByIds',
          method: 'POST',
          data: this.cartItems.map(item => item.id) // 直接发送ID数组
        })

        // 2. 构建订单数据
        const orderData = {
          items: this.cartItems.map(item => ({
            goodsId: item.id,
            quantity: item.quantity,
            price: item.price
          })),
          amount: this.calculateTotal(),
          discount: 0, // 默认无折扣
          actual: this.calculateTotal(), // 实际支付金额
          status: '待发货',
          time: new Date().toISOString(),
          payType: this.paymentType,
          payTime: new Date().toISOString(),
          businessId: this.getPrimaryBusinessId(), // 获取主要商家ID
          address: this.orderForm.address,
          phone: this.orderForm.phone,
          userId: this.user.id,
          user: this.user.name,
          comment: this.orderForm.comment,
          name: this.generateOrderName(goodsDetails), // 生成订单名称
          cover:this.form.img
        }

        // 3. 提交订单
        const res = await this.$request({
          url: '/orders/addOrder',
          method: 'POST',
          data: orderData
        })

        if (res.code === '200') {
          this.$message.success('订单创建成功')
          this.cartItems = []
          this.cartVisible = false
          this.paymentVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      } catch (error) {
        this.$message.error('订单创建失败: ' + (error.message || '未知错误'))
      }
    },

    // 获取主要商家ID（取第一个商品的商家）
    getPrimaryBusinessId() {
      return this.cartItems[0]?.businessId || null
    },

    // 生成订单名称
    generateOrderName(goodsDetails) {
      const mainGoods = goodsDetails[0] || {}
      const count = this.cartItems.length
      return `${mainGoods.name}等${count}件商品`
    },

    // 计算总价
    calculateTotal() {
      return this.cartItems.reduce((total, item) => {
        return total + (item.price * item.quantity)
      }, 0)
    },

    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/goods/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
          name:this.name,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.username = null
      this.name = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleFileSuccess(response, file, fileList) {
      // 把属性换成上传的图片的链接
      this.form.img = response.data
    },

    // 从购物车创建订单
    createOrderFromCart() {
      if (!this.cartItems.length) {
        this.$message.warning('购物车为空，请先添加商品')
        return
      }

      // 构建订单数据
      const orderData = {
        items: this.cartItems.map(item => ({
          goodsId: item.id,
          quantity: item.quantity,
          price: item.price
        })),
        amount: this.calculateTotal(),
        // 可以添加其他默认字段
        status: '待支付',
        time: new Date().toISOString()
      }

      this.$confirm(`确定生成订单吗？总金额: ${this.calculateTotal().toFixed(2)} 元`, '确认订单', {
        type: 'warning'
      }).then(() => {
        this.$request({
          url: '/orders/add',
          method: 'POST',
          data: orderData
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('订单创建成功')
            this.cartItems = [] // 清空购物车
            this.cartVisible = false // 关闭购物车弹窗
            this.load() // 刷新订单列表
          } else {
            this.$message.error(res.msg)
          }
        }).catch(err => {
          this.$message.error('订单创建失败: ' + (err.message || '未知错误'))
        })
      }).catch(() => {
        // 用户取消操作
      })
    },

    // 刷新订单列表
    loadOrders() {
      this.$request.get('/orders/list').then(res => {
        if (res.code === '200') {
          this.orders = res.data
        }
      })
    }

  }
}
</script>

<style scoped>

</style>
export default {
  common: {
    menuData: [{
      title: '数据治理',
      child: [{
        title: '注册配置中心',
        href: '/'
      }, {
        title: '配置管理',
        href: '/config-manage'
      }]
    }],
    connect: '已连接',
    connection: '连接',
    del: '删除',
    notify: {
      title: '提示',
      conSucMessage: '连接成功',
      conFailMessage: '连接失败',
      delSucMessage: '删除成功',
      delFailMessage: '删除失败',
      updateCompletedMessage: '更新成功',
      updateFaildMessage: '更新失败'
    }
  },
  login: {
    btnTxt: '登陆',
    labelUserName: '用户名',
    labelPassword: '密码'
  },
  btn: {
    submit: '提交',
    reset: '重置',
    cancel: '取消'
  },
  input: {
    pUserName: '请输入用户名',
    pPaasword: '请输入密码'
  },
  index: {
    btnTxt: '添加',
    registDialog: {
      title: '添加注册中心',
      name: '注册中心名称',
      centerType: '注册中心类型',
      address: '注册中心地址',
      orchestrationName: '数据治理实例',
      namespaces: '命名空间',
      btnConfirmTxt: '确定',
      btnCancelTxt: '取消'
    },
    table: {
      operate: '操作'
    },
    rules: {
      name: '请输入注册中心名称',
      centerType: '请选择注册中心类型',
      namespaces: '请输入命名空间',
      address: '请选输入注册中心地址',
      orchestrationName: '请输入数据治理实例名称'
    }
  }
}

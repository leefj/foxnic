## 版本：1.4.2.RELEASE ( 2021-12-03 )
### 新特性

1. 


### Bug 修复
1. 修复查询时，语句拼装未接入中间表查询条件的Bug
















## 版本：1.4.1.RELEASE ( 2021-11-29 )
### 新特性

1. 更新或插入时 Communications link failure 异常识别 (done)
2. 更新或插入时 Lock wait timeout exceeded 异常识别 (done)
3. exists 语句实现 (done)
4. 因逻辑删而在新增时引起的唯一键重复问题 (done)
5. 缓存的相关接口加入到服务


### Bug 修复
1. 修复DAO中因缓存判断引发的NPL异常 (done)
2. 字段名带下划线字段模糊搜索未支持的bug修复

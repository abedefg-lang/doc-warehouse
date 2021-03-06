package pers.tom.docwarehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import pers.tom.docwarehouse.model.dto.DocumentDto;
import pers.tom.docwarehouse.model.entity.Document;
import pers.tom.docwarehouse.model.param.DocumentParam;
import pers.tom.docwarehouse.model.query.DocumentQuery;
import pers.tom.docwarehouse.model.supports.PageParam;
import pers.tom.docwarehouse.model.supports.PageResult;


/**
 * @author lijia
 * @description
 * @date 2021-02-04 11:10
 */
public interface DocumentService extends IService<Document> {

    /**
     * 创建文档
     * @param documentParam 文档参数
     * @return 返回创建的文档
     */
    Document  create(DocumentParam documentParam);

    /**
     * 编辑文档
     * @param newDocument 新文档参数
     * @param documentId 主键
     * @return 返回是否编辑成功
     */
    boolean edit(DocumentParam newDocument, Long documentId);

    /**
     * 查询文档详情 包含文档内容
     * @param documentId 主键
     * @return 返回dto
     */
    DocumentDto details(Long documentId);

    /**
     * 分页查询文档
     * @param documentQuery 查询条件
     * @param pageParam 分页参数
     * @return 返回分页结果
     */
    PageResult<DocumentDto> pageBy(DocumentQuery documentQuery, PageParam pageParam);

    /**
     * 将文档内容 内容概述 回退到指定版本
     * @param versionId 版本id
     * @return 返回是否回退成功
     */
    boolean revert(Long versionId);

    /**
     * 合并分类  将原来分类中的文档 移动到新的分类中
     * @param oriCategoryId 原来的分类id
     * @param newCategoryId 新的分类id
     * @return 返回是否合并成功
     */
    boolean mergeCategory(Long oriCategoryId, Long newCategoryId);
}

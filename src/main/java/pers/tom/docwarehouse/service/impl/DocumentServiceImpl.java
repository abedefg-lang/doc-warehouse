package pers.tom.docwarehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.tom.docwarehouse.exception.ServiceException;
import pers.tom.docwarehouse.mapper.DocumentMapper;
import pers.tom.docwarehouse.model.dto.DocumentDto;
import pers.tom.docwarehouse.model.entity.Category;
import pers.tom.docwarehouse.model.entity.Document;
import pers.tom.docwarehouse.model.entity.DocumentVersion;
import pers.tom.docwarehouse.model.entity.OperationLog;
import pers.tom.docwarehouse.model.param.DocumentParam;
import pers.tom.docwarehouse.model.query.DocumentQuery;
import pers.tom.docwarehouse.model.supports.PageParam;
import pers.tom.docwarehouse.model.supports.PageResult;
import pers.tom.docwarehouse.service.CategoryService;
import pers.tom.docwarehouse.service.DocumentService;
import pers.tom.docwarehouse.service.DocumentVersionService;
import pers.tom.docwarehouse.service.OperationLogService;

/**
 * @author lijia
 * @description
 * @date 2021-02-04 11:11
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final OperationLogService logService;

    private final CategoryService categoryService;

    private final DocumentVersionService versionService;

    public DocumentServiceImpl(OperationLogService logService,
                               CategoryService categoryService,
                               DocumentVersionService versionService) {
        this.logService = logService;
        this.categoryService = categoryService;
        this.versionService = versionService;
    }

    @Override
    @Transactional
    public Document create(DocumentParam documentParam) {

        //判断文档标题是否存在
        String title = documentParam.getTitle();
        if(baseMapper.selectByTitle(title) != null){
            throw new ServiceException(title + "已存在");
        }

        Document document = documentParam.converterTo();
        baseMapper.insert(document);

        //新增文档版本
        versionService.save(new DocumentVersion(document.getDocumentId(), document.getContent(), document.getContentOverview()));

        //写入日志
        logService.saveLog(new OperationLog("创建文档: " + title));

        return document;
    }

    @Override
    @Transactional
    public boolean edit(DocumentParam documentParam, Long documentId) {

        Document oriDocument = baseMapper.selectById(documentId);
        if(oriDocument == null){
            throw new ServiceException("该文档不存在 请刷新重试");
        }

        //判断标题是否已存在
        String title = documentParam.getTitle();
        Document document = baseMapper.selectByTitle(title);
        if(document != null && !document.getDocumentId().equals(documentId)){
            throw new ServiceException(title + "已存在");
        }
        Document newDocument = documentParam.converterTo();
        newDocument.setDocumentId(documentId);
        baseMapper.updateById(newDocument);

        //判断文档内容是否有修改  如果发生修改添加历史版本
        if(!oriDocument.getContent().equals(newDocument.getContent())){
            versionService.save(new DocumentVersion(documentId, newDocument.getContent(), newDocument.getContentOverview()));
        }

        //写入日志
        logService.saveLog(new OperationLog("编辑文档: " + title));

        return true;
    }

    @Override
    public DocumentDto details(Long documentId) {

        Document document = baseMapper.selectById(documentId);
        if(document == null){
            throw new ServiceException("找不到文档 请刷新重试");
        }
        return converterTo(document);
    }

    @Override
    public PageResult<DocumentDto> pageBy(DocumentQuery documentQuery, PageParam pageParam) {

        Page<Document> page = new Page<>(pageParam.getPage(), pageParam.getPageSize(), pageParam.isSearchTotal());
        QueryWrapper<Document> queryWrapper = documentQuery.toQueryWrapper();

        //设置查询字段不包含内容
        queryWrapper.select("`document_id`", "`category_id`", "`title`", "`content_overview`", "`created_by`", "`creator_id`", "`create_time`", "`updated_by`", "`updater_id`", "`update_time`");
        baseMapper.selectPage(page, queryWrapper);

        return PageResult.fromIPage(page, this::converterTo);
    }

    @Override
    @Transactional
    public boolean revert(Long versionId) {

        DocumentVersion version = versionService.getById(versionId);
        if(version == null){
            throw new ServiceException("找不到该版本 请刷新重试");
        }

        //修改内容 内容概述
        Document document = new Document();
        document.setDocumentId(version.getDocumentId());
        document.setContent(version.getContent());
        document.setContentOverview(version.getContentOverview());

        if(!this.updateById(document)){
            throw new ServiceException("找不到该文档 请刷新重试");
        }

        //修改成功 添加日志
        logService.saveLog(new OperationLog("回退文档: " + document.getTitle()));
        return true;
    }

    @Override
    public boolean mergeCategory(Long oriCategoryId, Long newCategoryId) {

        return SqlHelper.retBool(baseMapper.replaceCategoryId(oriCategoryId, newCategoryId));
    }

    private DocumentDto converterTo(Document document){

        if(document != null){

            DocumentDto documentDto = new DocumentDto().converterFrom(document);
            Category category = categoryService.getById(document.getCategoryId());
            documentDto.setCategoryName(category == null ? "" : category.getName());
            return documentDto;
        }

        return null;
    }
}

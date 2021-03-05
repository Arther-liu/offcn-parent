package com.offcn.project.service.impl;

import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {
    @Resource
    private TReturnMapper tReturnMapper;
    @Resource
    private TProjectMapper projectMapper;
    @Resource
    private TProjectImagesMapper projectImagesMapper;
    @Resource
    private TTagMapper tagMapper;
    @Resource
    private TTypeMapper typeMapper;

    @Override
    public List<TReturn> getReturnList(Integer projectId) {
        TReturnExample tReturnExample = new TReturnExample();
        tReturnExample.createCriteria().andProjectidEqualTo(projectId);
        List<TReturn> tReturns = tReturnMapper.selectByExample(tReturnExample);
        return tReturns;
    }
    /**
     * 获取系统中所有项目
     *
     * @return
     */
    @Override
    public List<TProject> findAllProject() {
        return projectMapper.selectByExample(null);
    }

    /**
     * 获取项目图片
     *
     * @param id
     * @return
     */
    @Override
    public List<TProjectImages> getProjectImages(Integer id) {
        TProjectImagesExample example = new TProjectImagesExample();
        example.createCriteria().andProjectidEqualTo(id);
        return projectImagesMapper.selectByExample(example);
    }

    /**
     * 获取项目信息
     *
     * @param projectId
     * @return
     */
    @Override
    public TProject findProjectInfo(Integer projectId) {
        TProject project = projectMapper.selectByPrimaryKey(projectId);
        return project;
    }

    /**
     * 获得项目标签
     *
     * @return
     */
    @Override
    public List<TTag> findAllTag() {
        return tagMapper.selectByExample(null);
    }

    /**
     * 获取项目分类
     *
     * @return
     */
    @Override
    public List<TType> findAllType() {
        return typeMapper.selectByExample(null);
    }
    @Override
    public TReturn findReturnInfo(Integer returnId) {
        return tReturnMapper.selectByPrimaryKey(returnId);
    }

}

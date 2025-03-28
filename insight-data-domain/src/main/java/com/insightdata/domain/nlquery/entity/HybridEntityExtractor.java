package com.insightdata.domain.nlquery.entity;

import com.insightdata.domain.nlquery.QueryContext;
import com.insightdata.domain.nlquery.preprocess.PreprocessedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合实体提取器
 * 结合规则和元数据的实体提取
 */
@Component
public class HybridEntityExtractor implements EntityExtractor {
    
    @Autowired
    private RuleBasedEntityExtractor ruleBasedExtractor;
    
    @Autowired
    private MetadataBasedEntityExtractor metadataBasedExtractor;
    
    @Override
    public List<EntityTag> extract(PreprocessedText preprocessedText) {
        return this.extract(preprocessedText, new EntityExtractionContext());
    }
    
    @Override
    public List<EntityTag> extract(PreprocessedText preprocessedText, EntityExtractionContext context) {
        // 获取规则提取的实体
        List<EntityTag> ruleEntities = ruleBasedExtractor.extract(preprocessedText, context);
        
        // 获取元数据提取的实体
        List<EntityTag> metadataEntities = metadataBasedExtractor.extract(preprocessedText, context);
        
        // 合并实体
        return mergeEntities(ruleEntities, metadataEntities);
    }

    @Override
    public List<EntityTag> validate(List<EntityTag> entities, QueryContext context) {
        return List.of();
    }

    @Override
    public List<EntityTag> merge(List<EntityTag> entities) {
        return List.of();
    }

    /**
     * 合并实体
     * 
     * @param ruleEntities 规则提取的实体
     * @param metadataEntities 元数据提取的实体
     * @return 合并后的实体
     */
    private List<EntityTag> mergeEntities(List<EntityTag> ruleEntities, List<EntityTag> metadataEntities) {
        // 创建结果列表
        List<EntityTag> mergedEntities = new ArrayList<>();
        
        // 创建位置映射，用于检测重叠
        Map<Integer, EntityTag> positionMap = new HashMap<>();
        
        // 先添加元数据提取的实体，因为它们通常更准确
        for (EntityTag entity : metadataEntities) {
            // 添加到结果列表
            mergedEntities.add(entity);
            
            // 添加到位置映射
            for (int i = entity.getStartOffset(); i < entity.getEndOffset(); i++) {
                positionMap.put(i, entity);
            }
        }
        
        // 再添加规则提取的实体，但要避免重叠
        for (EntityTag entity : ruleEntities) {
            // 检查是否与已有实体重叠
            boolean overlap = false;
            for (int i = entity.getStartOffset(); i < entity.getEndOffset(); i++) {
                if (positionMap.containsKey(i)) {
                    overlap = true;
                    
                    // 如果重叠的实体置信度较低，则替换
                    EntityTag existingEntity = positionMap.get(i);
                    if (entity.getConfidence() > existingEntity.getConfidence()) {
                        // 从结果列表中移除已有实体
                        mergedEntities.remove(existingEntity);
                        
                        // 从位置映射中移除已有实体
                        for (int j = existingEntity.getStartOffset(); j < existingEntity.getEndOffset(); j++) {
                            positionMap.remove(j);
                        }
                        
                        // 添加新实体
                        mergedEntities.add(entity);
                        
                        // 添加到位置映射
                        for (int j = entity.getStartOffset(); j < entity.getEndOffset(); j++) {
                            positionMap.put(j, entity);
                        }
                    }
                    
                    break;
                }
            }
            
            // 如果没有重叠，则直接添加
            if (!overlap) {
                mergedEntities.add(entity);
                
                // 添加到位置映射
                for (int i = entity.getStartOffset(); i < entity.getEndOffset(); i++) {
                    positionMap.put(i, entity);
                }
            }
        }
        
        return mergedEntities;
    }
}
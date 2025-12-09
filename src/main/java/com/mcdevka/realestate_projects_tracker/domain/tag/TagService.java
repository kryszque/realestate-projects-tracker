package com.mcdevka.realestate_projects_tracker.domain.tag;

import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {return tagRepository.findAll();}

    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag with ID " + id + " not found!"));
    }
    @CheckAccess(ProjectPermissions.CAN_CREATE)
    public Tag createTag(Tag inputTag) {
        if (tagRepository.existsByName(inputTag.getName())) {
            throw new IllegalArgumentException("Tag with name " + inputTag.getName() + " already exists!");
        }

        Tag createdTag = new Tag();
        createdTag.setName(inputTag.getName());

        return tagRepository.save(createdTag);
    }

    @CheckAccess(ProjectPermissions.CAN_DELETE)
    public Tag archiveTag(Long id){
        Tag archivedTag = getTagById(id);
        archivedTag.setState("archived");
        return tagRepository.save(archivedTag);
    }
}

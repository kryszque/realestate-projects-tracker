package com.mcdevka.realestate_projects_tracker.domain.tag;

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

    public Tag createTag(Tag inputTag) {
        Tag createdTag = new Tag();

        createdTag.setName(inputTag.getName());

        return tagRepository.save(createdTag);
    }

    public Tag archiveTag(Long id){
        Tag archivedTag = getTagById(id);
        archivedTag.setState("archived");
        return tagRepository.save(archivedTag);
    }
}

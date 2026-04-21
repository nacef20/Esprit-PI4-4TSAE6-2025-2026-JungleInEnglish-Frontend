package tn.esprit.gestiongroupechat.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestiongroupechat.Dto.GroupMemberDTO;
import tn.esprit.gestiongroupechat.service.InGroupMembre;

import java.util.List;

@RestController
@RequestMapping("/membreGroupChat")
@RequiredArgsConstructor
public class GroupMembreController {

    private final InGroupMembre groupMemberService;


    @PostMapping("/add")
    public ResponseEntity<GroupMemberDTO> addMember(@RequestBody @Valid GroupMemberDTO dto) {
        GroupMemberDTO created = groupMemberService.addMember(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<GroupMemberDTO>> getMembersByGroup(@PathVariable Long groupId) {
        List<GroupMemberDTO> members = groupMemberService.getMembersByGroup(groupId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable String userId) {

        groupMemberService.removeMember(groupId, userId);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/is-member")
    public ResponseEntity<Boolean> isMember(
            @RequestParam Long groupId,
            @RequestParam String userId) {

        boolean exists = groupMemberService.isMember(groupId, userId);
        return ResponseEntity.ok(exists);
    }



}
